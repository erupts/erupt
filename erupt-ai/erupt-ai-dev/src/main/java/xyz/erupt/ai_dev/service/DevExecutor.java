package xyz.erupt.ai_dev.service;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.ai_dev.constant.AgentRole;
import xyz.erupt.ai_dev.constant.EventKind;
import xyz.erupt.ai_dev.constant.RequirementStage;
import xyz.erupt.ai_dev.constant.SessionStatus;
import xyz.erupt.ai_dev.notice.DevNoticeEvent;
import xyz.erupt.ai_dev.notice.DevNotices;
import xyz.erupt.ai_dev.model.DevAgentEvent;
import xyz.erupt.ai_dev.model.DevAgentSession;
import xyz.erupt.ai_dev.model.DevProject;
import xyz.erupt.ai_dev.model.DevRequirement;
import xyz.erupt.ai_dev.prop.EruptAiDevProp;
import xyz.erupt.ai_dev.runner.CodingAgentRunner;
import xyz.erupt.ai_dev.runner.DevRunners;
import xyz.erupt.ai_dev.runner.DevAgentProcess;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.helper.UpmsTenant;

import com.google.gson.JsonObject;
import org.springframework.core.env.Environment;
import xyz.erupt.ai_dev.mcp.DevMcpGrants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

/**
 * Runs approved requirements. A poller claims QUEUED requirements (a conditional update, so two instances
 * never take the same one), and each claimed requirement runs on a worker: workspace, developer agent,
 * optionally the QA agent, then ACCEPTANCE for a human to review. Every agent message is stored as it
 * arrives, so the log can be followed live and survives the process.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Slf4j
@Service
public class DevExecutor {

    private record RunResult(boolean ok, boolean stopped, String text, String message, String verdict) {
    }

    // content kept per log line; a tool result can be a whole file
    private static final int EVENT_LENGTH = 20_000;

    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor(r -> daemon(r, "erupt-ai-dev-poller"));

    private final ExecutorService workers = Executors.newCachedThreadPool(r -> daemon(r, "erupt-ai-dev-run"));

    private final Set<Long> active = ConcurrentHashMap.newKeySet();

    // the log of every session in flight, by session row, so the MCP endpoint writes into the same sequence
    private final Map<Long, SessionLog> logs = new ConcurrentHashMap<>();

    @Resource
    private DevNotices devNotices;

    @Resource
    private EruptAiDevProp eruptAiDevProp;

    @Resource
    private DevTx devTx;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private DevWorkspaceService devWorkspaceService;

    @Resource
    private DevAgentProcess devAgentProcess;

    @Resource
    private DevRunners devRunners;

    @Resource
    private DevMcpGrants devMcpGrants;

    @Resource
    private DevLayout devLayout;

    @Resource
    private Environment environment;

    private static Thread daemon(Runnable r, String name) {
        Thread thread = new Thread(r, name);
        thread.setDaemon(true);
        return thread;
    }

    @PreDestroy
    void shutdown() {
        poller.shutdownNow();
        workers.shutdownNow();
    }

    public boolean enabled() {
        return eruptAiDevProp.getExecutor().isEnabled();
    }

    /**
     * Starts polling on an instance with the executor enabled. Runs a restart cut short are failed first:
     * no process of theirs survived it.
     */
    public void start() {
        if (!this.enabled()) return;
        for (Pending r : this.across(RequirementStage.RUNNING, RequirementStage.VERIFYING)) {
            this.fail(r.tenant(), r.id(), "Interrupted by a server restart, retry to continue");
        }
        poller.scheduleWithFixedDelay(this::poll, 5, eruptAiDevProp.getExecutor().getPollSeconds(), TimeUnit.SECONDS);
    }

    /**
     * Checks the queue now, e.g. right after an approval.
     */
    public void kick() {
        if (this.enabled()) poller.execute(this::poll);
    }

    private void poll() {
        try {
            for (Pending r : this.across(RequirementStage.QUEUED)) {
                if (active.size() >= eruptAiDevProp.getExecutor().getMaxConcurrent()) return;
                if (!active.add(r.id())) continue;
                String tenant = r.tenant();
                Long id = r.id();
                workers.execute(() -> {
                    try {
                        this.execute(tenant, id);
                    } finally {
                        active.remove(id);
                        this.kick();
                    }
                });
            }
        } catch (RuntimeException e) {
            log.warn("Polling the requirement queue failed: {}", e.getMessage(), e);
        }
    }

    // a requirement to act on, with the tenant of its project that every later step binds to
    private record Pending(Long id, String tenant) {
    }

    // requirements in these stages in every tenant, oldest first
    private List<Pending> across(RequirementStage... stages) {
        return devTx.call(UpmsTenant.PLATFORM, () -> eruptDao.lambdaQuery(DevRequirement.class).allTenants()
                .in(DevRequirement::getStage, List.of(stages)).list().stream()
                .sorted(Comparator.comparing(DevRequirement::getUpdateTime, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(r -> new Pending(r.getId(), r.getProject().getTenantId()))
                .toList());
    }

    /**
     * Runs one requirement to ACCEPTANCE or FAILED, if it is still queued.
     */
    public void execute(String tenant, Long id) {
        boolean claimed = devTx.call(tenant, () -> eruptDao.getEntityManager().createQuery(
                        "update DevRequirement r set r.stage = :running, r.statusMessage = null, r.progress = 0, r.phase = null " +
                                "where r.id = :id and r.stage = :queued")
                .setParameter("running", RequirementStage.RUNNING).setParameter("queued", RequirementStage.QUEUED)
                .setParameter("id", id).executeUpdate() == 1);
        if (!claimed) return;
        try {
            DevRunContext ctx = devTx.call(tenant, () -> {
                DevRequirement r = eruptDao.find(DevRequirement.class, id);
                DevRunContext context = DevRunContext.of(eruptDao.find(DevProject.class, r.getProject().getId()), r);
                // the instruction is handed to this run only
                r.setInstruction(null);
                return context;
            });
            DevRunContext.Agent developer = ctx.agent(AgentRole.DEVELOPER)
                    .orElseThrow(() -> new IllegalStateException("The requirement has no executor"));
            Path workspace = devWorkspaceService.prepare(ctx);
            // continue the last session when the CLI can, else start over with the whole requirement
            boolean resumable = devRunners.find(developer.executor()).map(CodingAgentRunner::supportsResume).orElse(false);
            String resume = resumable ? this.lastSessionId(tenant, id, AgentRole.DEVELOPER) : null;
            RunResult dev = this.runAgent(ctx, developer, workspace,
                    null == resume ? DevPrompts.developer(ctx) : DevPrompts.resume(ctx), resume);
            if (!dev.ok()) {
                this.fail(tenant, id, dev.message());
                return;
            }
            devWorkspaceService.commitLeftovers(ctx, devRunners.find(developer.executor()).map(CodingAgentRunner::label).orElse(developer.name()));
            String verdict = null;
            Optional<DevRunContext.Agent> qa = ctx.agent(AgentRole.QA);
            if (qa.isPresent()) {
                this.update(tenant, id, r -> {
                    r.setStage(RequirementStage.VERIFYING);
                    r.setPhase("verify");
                });
                RunResult check = this.runAgent(ctx, qa.get(), workspace, DevPrompts.qa(ctx), null);
                if (check.stopped()) {
                    this.fail(tenant, id, check.message());
                    return;
                }
                // the verdict the QA agent reported through MCP, else its marker line
                verdict = !check.ok() ? "FAIL " + check.message()
                        : StringUtils.firstNonBlank(check.verdict(), DevPrompts.verdict(check.text()), "FAIL no verdict reported");
            }
            String finalVerdict = verdict;
            this.update(tenant, id, r -> {
                r.setStage(RequirementStage.ACCEPTANCE);
                r.setProgress(100);
                r.setPhase("review");
                r.setVerdict(StringUtils.abbreviate(finalVerdict, AnnotationConst.REMARK_LENGTH));
                devNotices.publishFromExecutor(r, DevNoticeEvent.Kind.ACCEPTANCE);
            });
        } catch (Exception e) {
            log.warn("Run of requirement {} failed: {}", id, e.getMessage(), e);
            this.fail(tenant, id, e.getMessage());
        }
    }

    private RunResult runAgent(DevRunContext ctx, DevRunContext.Agent agent, Path workspace, String prompt, String resume) throws Exception {
        CodingAgentRunner runner = devRunners.find(agent.executor())
                .orElseThrow(() -> new IllegalStateException("Unknown executor " + agent.executor()));
        if (!devRunners.installed(runner)) {
            throw new IllegalStateException(runner.label() + " is not installed on this server (" + runner.binary() + " not found)");
        }
        Long sessionRow = devTx.call(ctx.tenant(), () -> {
            DevAgentSession session = new DevAgentSession();
            session.setProject(eruptDao.getEntityManager().getReference(DevProject.class, ctx.projectId()));
            session.setRequirement(eruptDao.getEntityManager().getReference(DevRequirement.class, ctx.requirementId()));
            session.setRole(agent.role());
            session.setAgentName(agent.role() == AgentRole.QA ? runner.label() + " QA" : runner.label());
            session.setExecutor(agent.executor());
            session.setStatus(SessionStatus.RUNNING);
            session.setSessionId(resume);
            session.setStartedAt(LocalDateTime.now());
            eruptDao.persist(session);
            return session.getId();
        });
        SessionLog sessionLog = new SessionLog(ctx, sessionRow, agent.role() == AgentRole.DEVELOPER);
        logs.put(sessionRow, sessionLog);
        Long creator = devTx.call(ctx.tenant(), () -> this.creatorOf(eruptDao.find(DevRequirement.class, ctx.requirementId())));
        String token = devMcpGrants.issue(ctx.tenant(), ctx.projectId(), ctx.requirementId(), sessionRow, agent.role(), creator);
        Path mcpConfig = this.mcpConfig(token);
        DevAgentProcess.Outcome outcome;
        DevMcpGrants.Grant grant;
        try {
            sessionLog.event(EventKind.SYSTEM, "prompt", prompt);
            if (null == mcpConfig) sessionLog.event(EventKind.SYSTEM, "mcp", "No MCP endpoint address: progress comes from marker lines");
            outcome = devAgentProcess.run(ctx.requirementId(), runner,
                    new CodingAgentRunner.Spec(workspace, prompt, agent.model(), resume, mcpConfig,
                            null == mcpConfig ? null : this.mcpUrl(), null == mcpConfig ? null : token), sessionLog);
        } finally {
            grant = devMcpGrants.revoke(token);
            logs.remove(sessionRow);
            if (null != mcpConfig) Files.deleteIfExists(mcpConfig);
        }
        // a CLI without a closing result event is judged by its exit code, its last text the answer
        if (null == sessionLog.success && !outcome.stopped() && !outcome.timedOut() && outcome.exitCode() == 0) {
            sessionLog.result(true, null, null, null);
        }
        boolean ok = !outcome.stopped() && !outcome.timedOut() && outcome.exitCode() == 0 && Boolean.TRUE.equals(sessionLog.success);
        String message = outcome.stopped() ? "Stopped"
                : outcome.timedOut() ? "Timed out after " + eruptAiDevProp.getExecutor().getRunTimeoutMinutes() + " minutes"
                : null == sessionLog.success ? "The agent exited with code " + outcome.exitCode() + " without a result: " + outcome.stderrTail().strip()
                : !sessionLog.success ? "The agent reported a failure: " + StringUtils.defaultString(sessionLog.text)
                : null;
        if (null != message && !ok) sessionLog.event(EventKind.ERROR, null, message);
        SessionStatus status = outcome.stopped() ? SessionStatus.STOPPED : ok ? SessionStatus.SUCCEEDED : SessionStatus.FAILED;
        devTx.run(ctx.tenant(), () -> {
            DevAgentSession session = eruptDao.find(DevAgentSession.class, sessionRow);
            session.setStatus(status);
            session.setSessionId(StringUtils.defaultIfBlank(sessionLog.sessionId, resume));
            session.setSummary(sessionLog.text);
            session.setCostUsd(sessionLog.costUsd);
            session.setTokens(sessionLog.tokens);
            session.setEndedAt(LocalDateTime.now());
        });
        return new RunResult(ok, outcome.stopped(), sessionLog.text, message, null == grant ? null : grant.verdict());
    }

    /**
     * The agent's MCP client configuration for one run, readable by the server account only; null when the
     * endpoint's address is unknown (no web server, e.g. a batch process).
     */
    /**
     * The user a requirement's run acts for in erupt's tools: its creator, or for a requirement from before
     * creators were kept by id, the one user its creator's name belongs to. Null when that is not clear.
     */
    private Long creatorOf(DevRequirement r) {
        if (null == r) return null;
        if (null != r.getCreator()) return r.getCreator().getId();
        if (StringUtils.isBlank(r.getCreateBy())) return null;
        List<EruptUser> named = eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getName, r.getCreateBy()).limit(2).list();
        if (named.size() != 1) return null;
        // kept, so the next run and the notices find it directly
        r.setCreator(named.get(0));
        return named.get(0).getId();
    }

    // erupt-ai's MCP endpoint as the agent reaches it, null when unknown (no web server)
    private String mcpUrl() {
        String url = StringUtils.trimToNull(eruptAiDevProp.getExecutor().getMcpUrl());
        if (null != url) return url;
        String port = environment.getProperty("local.server.port");
        if (null == port) return null;
        return "http://127.0.0.1:" + port + StringUtils.defaultString(environment.getProperty("server.servlet.context-path")) + "/mcp";
    }

    private Path mcpConfig(String token) throws IOException {
        String url = this.mcpUrl();
        if (null == url) return null;
        JsonObject server = new JsonObject();
        server.addProperty("type", "http");
        server.addProperty("url", url);
        JsonObject headers = new JsonObject();
        headers.addProperty("Authorization", "Bearer " + token);
        server.add("headers", headers);
        JsonObject servers = new JsonObject();
        servers.add(DevMcpGrants.SERVER, server);
        JsonObject config = new JsonObject();
        config.add("mcpServers", servers);
        Path dir = devLayout.root().resolve("run");
        Files.createDirectories(dir);
        Path file = dir.resolve(UUID.randomUUID() + ".json");
        Files.writeString(file, config.toString(), StandardCharsets.UTF_8);
        try {
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException ignored) {
            // not a POSIX file system: the directory's own permissions apply
        }
        return file;
    }

    /**
     * A log line from outside the CLI's own output (the MCP endpoint), in the session's sequence.
     */
    public void event(Long sessionRow, EventKind kind, String name, String content) {
        SessionLog log = logs.get(sessionRow);
        if (null != log) log.event(kind, name, content);
    }

    /**
     * Raises a running requirement's progress; a lower or repeated report is ignored. 100 is kept for the end
     * of the run.
     */
    public void reportProgress(String tenant, Long requirementId, int percent, String phase) {
        int value = Math.min(99, Math.max(0, percent));
        devTx.run(tenant, () -> eruptDao.getEntityManager().createQuery(
                        "update DevRequirement r set r.progress = :p, r.phase = coalesce(:phase, r.phase) " +
                                "where r.id = :id and r.stage = :running and (r.progress is null or r.progress < :p)")
                .setParameter("p", value).setParameter("phase", phase).setParameter("id", requirementId)
                .setParameter("running", RequirementStage.RUNNING).executeUpdate());
    }

    private String lastSessionId(String tenant, Long requirementId, AgentRole role) {
        return devTx.call(tenant, () -> eruptDao.getEntityManager().createQuery(
                        "select s.sessionId from DevAgentSession s where s.requirement.id = :id and s.role = :role " +
                                "and s.sessionId is not null order by s.id desc", String.class)
                .setParameter("id", requirementId).setParameter("role", role)
                .setMaxResults(1).getResultList().stream().findFirst().orElse(null));
    }

    private void fail(String tenant, Long id, String message) {
        this.update(tenant, id, r -> {
            r.setStage(RequirementStage.FAILED);
            r.setStatusMessage(StringUtils.abbreviate(message, AnnotationConst.REMARK_LENGTH));
            devNotices.publishFromExecutor(r, DevNoticeEvent.Kind.FAILED);
        });
    }

    private void update(String tenant, Long id, java.util.function.Consumer<DevRequirement> change) {
        devTx.run(tenant, () -> {
            DevRequirement r = eruptDao.find(DevRequirement.class, id);
            if (null != r) change.accept(r);
        });
    }

    /**
     * Stores every message of one agent session as it arrives, and turns the developer's PROGRESS lines into
     * the requirement's progress.
     */
    private class SessionLog implements CodingAgentRunner.Listener {

        private final DevRunContext ctx;

        private final Long sessionRow;

        private final boolean reportsProgress;

        private final AtomicInteger seq = new AtomicInteger();

        private String sessionId;

        private Boolean success;

        private String text;

        // the latest text the agent printed: the answer of a CLI that reports none
        private String lastText;

        private Double costUsd;

        private Long tokens;

        SessionLog(DevRunContext ctx, Long sessionRow, boolean reportsProgress) {
            this.ctx = ctx;
            this.sessionRow = sessionRow;
            this.reportsProgress = reportsProgress;
        }

        @Override
        public void session(String sessionId) {
            if (null != this.sessionId) return;
            this.sessionId = sessionId;
            devTx.run(ctx.tenant(), () -> eruptDao.find(DevAgentSession.class, sessionRow).setSessionId(sessionId));
        }

        @Override
        public void event(EventKind kind, String name, String content) {
            int n = seq.incrementAndGet();
            devTx.run(ctx.tenant(), () -> {
                DevAgentEvent event = new DevAgentEvent();
                event.setProject(eruptDao.getEntityManager().getReference(DevProject.class, ctx.projectId()));
                event.setSession(eruptDao.getEntityManager().getReference(DevAgentSession.class, sessionRow));
                event.setSeq(n);
                event.setKind(kind);
                event.setName(StringUtils.abbreviate(name, AnnotationConst.CODE_LENGTH * 4));
                event.setContent(StringUtils.abbreviate(content, EVENT_LENGTH));
                event.setCreatedAt(LocalDateTime.now());
                eruptDao.persist(event);
            });
            if (kind == EventKind.TEXT && StringUtils.isNotBlank(content)) lastText = content;
            if (reportsProgress && kind == EventKind.TEXT && null != content) this.progress(content);
        }

        private void progress(String content) {
            Matcher m = DevPrompts.PROGRESS.matcher(content);
            Integer percent = null;
            String phase = null;
            while (m.find()) {
                percent = Integer.parseInt(m.group(1));
                phase = StringUtils.trimToNull(m.group(2));
            }
            if (null != percent) DevExecutor.this.reportProgress(ctx.tenant(), ctx.requirementId(), percent, phase);
        }

        @Override
        public void result(boolean success, String text, Double costUsd, Long tokens) {
            this.success = success;
            this.text = StringUtils.defaultIfBlank(text, lastText);
            this.costUsd = costUsd;
            this.tokens = tokens;
            this.event(EventKind.RESULT, success ? "success" : "error", text);
        }
    }

}
