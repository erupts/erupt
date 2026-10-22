package xyz.erupt.test.aidev;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.erupt.ai_dev.constant.*;
import xyz.erupt.ai_dev.notice.DevNoticeListener;
import xyz.erupt.ai_dev.runner.ClaudeCodeRunner;
import xyz.erupt.ai_dev.model.*;
import xyz.erupt.ai_dev.prop.EruptAiDevProp;
import xyz.erupt.ai_dev.proxy.DevRequirementOperation;
import xyz.erupt.ai_dev.service.DevExecutor;
import xyz.erupt.ai_dev.service.DevLayout;
import xyz.erupt.ai_dev.service.DevRepoService;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.scope.EruptScopeService;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.git.EruptGit;
import xyz.erupt.git.GitAuth;
import xyz.erupt.git.GitCli;
import xyz.erupt.git.GitException;
import xyz.erupt.git.GitProp;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptSessionService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A requirement from approval to push, with a stand-in for the claude CLI: a shell script that prints
 * Claude Code's stream-json and commits a change in every worktree it finds, or plays QA and passes.
 */
class AiDevExecutorTest extends EruptApplicationTests {

    private static final Map<String, String> IDENT = Map.of(
            "GIT_AUTHOR_NAME", "seed", "GIT_AUTHOR_EMAIL", "seed@test", "GIT_COMMITTER_NAME", "seed", "GIT_COMMITTER_EMAIL", "seed@test");

    private static final String FAKE_CLAUDE = """
            #!/bin/sh
            prompt=$(cat)
            cfg=""; prev=""
            for a in "$@"; do [ "$prev" = "--mcp-config" ] && cfg="$a"; prev="$a"; done
            case "$*" in *--strict-mcp-config*mcp__erupt*) ;; *) echo "mcp flags missing: $*" >&2; exit 4 ;; esac
            url=$(sed -E 's/.*"url":"([^"]+)".*/\\1/' "$cfg")
            auth=$(sed -E 's/.*"Authorization":"([^"]+)".*/\\1/' "$cfg")
            mcp() { curl -s -X POST -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" -H "Authorization: $auth" --data "$1" "$url"; }
            mcp '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"fake","version":"1"}}}' > /dev/null
            mcp '{"jsonrpc":"2.0","method":"notifications/initialized"}' > /dev/null
            tools=$(mcp '{"jsonrpc":"2.0","id":2,"method":"tools/list"}')
            printf '%s\n' '{"type":"system","subtype":"init","session_id":"sess-42","model":"fake"}'
            case "$prompt" in
              *please-fail*)
                printf '%s\n' '{"type":"result","subtype":"error_during_execution","is_error":true,"result":"cannot do it"}'
                exit 1 ;;
              *"QA agent"*)
                case "$tools" in *devReportVerdict*) ;; *) exit 3 ;; esac
                mcp '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"devReportVerdict","arguments":{"pass":true,"reason":"verified over MCP"}}}' > /dev/null
                printf '%s\n' '{"type":"result","subtype":"success","is_error":false,"result":"Checked.\\nVERDICT: PASS","session_id":"qa-1"}'
                exit 0 ;;
            esac
            case "$tools" in *devReportProgress*) ;; *) exit 3 ;; esac
            mcp '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"devReportProgress","arguments":{"percent":55,"phase":"implement","note":"halfway"}}}' > /dev/null
            printf '%s\n' '{"type":"assistant","message":{"content":[{"type":"text","text":"Working on it"},{"type":"tool_use","name":"Bash","input":{"command":"git commit"}}]}}'
            for d in */; do
              [ -e "$d.git" ] || continue
              echo "changed by the agent" >> "${d}CHANGE.txt"
              (cd "$d" && git add -A && git commit -q -m "feat: change" -m "Requirement: REQ-1")
            done
            printf '%s\n' '{"type":"user","message":{"content":[{"type":"tool_result","content":"ok"}]}}'
            printf '%s\n' '{"type":"result","subtype":"success","is_error":false,"result":"Changed every repository.","total_cost_usd":0.01,"usage":{"input_tokens":10,"output_tokens":5},"session_id":"sess-42"}'
            """;

    @Resource
    private EruptModifyService eruptModifyService;

    @Resource
    private EruptSessionService eruptSessionService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private DevRepoService devRepoService;

    @Resource
    private DevExecutor devExecutor;

    @Resource
    private DevRequirementOperation devRequirementOperation;

    @Resource
    private DevLayout devLayout;

    @Resource
    private EruptGit eruptGit;

    @Resource
    private GitProp gitProp;

    @Resource
    private EruptAiDevProp eruptAiDevProp;

    private final String mark = UUID.randomUUID().toString().substring(0, 6);

    private final List<Object> users = new ArrayList<>();

    private Path tmp;

    private EruptUser admin;

    private Map<String, EruptAiDevProp.Cli> savedClis;

    private Map<String, String> savedEnv;

    @BeforeEach
    void setUp() throws IOException {
        try {
            eruptGit.version();
        } catch (GitException e) {
            Assumptions.abort("git is not installed");
        }
        Assumptions.assumeTrue(Files.isExecutable(Path.of("/bin/sh")), "needs a POSIX shell");
        gitProp.setAllowLocalRemote(true);
        tmp = Files.createTempDirectory("ai-dev-exec");
        Path script = tmp.resolve("fake-claude");
        Files.writeString(script, FAKE_CLAUDE);
        Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rwx------"));
        savedClis = eruptAiDevProp.getExecutor().getClis();
        savedEnv = eruptAiDevProp.getExecutor().getEnv();
        EruptAiDevProp.Cli claude = new EruptAiDevProp.Cli();
        claude.setBinary(script.toString());
        claude.setArgs(new ArrayList<>());
        Map<String, EruptAiDevProp.Cli> clis = new LinkedHashMap<>();
        clis.put(ClaudeCodeRunner.CODE, claude);
        eruptAiDevProp.getExecutor().setClis(clis);
        // the agent starts from a clean environment: its git identity comes from configuration
        eruptAiDevProp.getExecutor().setEnv(new LinkedHashMap<>(IDENT));
        admin = transactionTemplate.execute(tx -> {
            EruptUser user = new EruptUser();
            user.setAccount("exec-" + mark);
            user.setName("exec");
            user.setIsAdmin(true);
            user.setStatus(true);
            eruptDao.persist(user);
            users.add(user);
            return user;
        });
    }

    @AfterEach
    void tearDown() {
        gitProp.setAllowLocalRemote(false);
        if (null != savedClis) eruptAiDevProp.getExecutor().setClis(savedClis);
        if (null != savedEnv) eruptAiDevProp.getExecutor().setEnv(savedEnv);
        MetaContext.remove();
        RequestContextHolder.resetRequestAttributes();
        transactionTemplate.executeWithoutResult(tx -> {
            for (Class<?> type : List.of(DevAgentEvent.class, DevAgentSession.class, DevRequirementDelivery.class, DevRequirement.class)) {
                eruptDao.getEntityManager().createQuery("delete from " + type.getSimpleName() + " x where x.project.code like :c")
                        .setParameter("c", "%" + mark).executeUpdate();
            }
            eruptDao.lambdaQuery(DevProject.class).like(DevProject::getCode, mark).list().forEach(eruptDao::delete);
            users.forEach(it -> eruptDao.delete(eruptDao.getEntityManager().merge(it)));
        });
    }

    // ------------------------------------------------------------------ fixtures

    private Path remote(String name) throws IOException {
        GitCli cli = eruptGit.getCli();
        Path remote = tmp.resolve(name + ".git"), seed = tmp.resolve(name + "-seed");
        cli.run(null, "init", "--bare", "--initial-branch=develop", remote.toString());
        cli.run(null, "clone", remote.toUri().toString(), seed.toString());
        cli.run(seed, "checkout", "-b", "develop");
        Files.writeString(seed.resolve("README.md"), name + "\n");
        cli.run(seed, "add", "--all");
        cli.run(seed, GitAuth.NONE, IDENT, "commit", "-m", "init");
        cli.run(seed, "push", "origin", "develop");
        return remote;
    }

    private void as(Class<?> erupt, Long project) {
        String token = "exec-" + mark;
        MetaUserinfo info = new MetaUserinfo();
        info.setId(admin.getId());
        info.setAccount(admin.getAccount());
        info.setSuperAdmin(true);
        eruptSessionService.put(SessionKey.USER_INFO + token, GsonFactory.getGson().toJson(info), 10, TimeUnit.MINUTES);
        eruptSessionService.putMap(SessionKey.MENU_VALUE_MAP + token, Map.of("ai-dev-test", "{}"), 10, TimeUnit.MINUTES);
        MetaContext.remove();
        MetaContext.register(new MetaUser(admin.getId(), admin.getAccount(), admin.getName()));
        MetaContext.register(new MetaErupt(erupt.getSimpleName()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN, token);
        if (null != project) {
            request.addHeader(EruptScopeService.HEADER,
                    URLEncoder.encode("{\"DevProjectScope\":{\"project\":\"" + project + "\"}}", StandardCharsets.UTF_8));
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private DevProject project(Path web, Path api) {
        JsonObject json = new JsonObject();
        json.addProperty("code", "exec-" + mark);
        json.addProperty("name", "Exec " + mark);
        json.addProperty("agentsMd", "# House rules\nKeep it small.");
        // one user plays every part here
        json.addProperty("selfReview", true);
        JsonArray repos = new JsonArray();
        repos.add(repo("web", web, null));
        // api delivers straight onto develop: rebase, then fast-forward push
        repos.add(repo("api", api, "develop"));
        json.add("repos", repos);
        json.add("members", new JsonArray());
        this.as(DevProject.class, null);
        transactionTemplate.executeWithoutResult(tx -> eruptModifyService.insertEruptData(EruptCoreService.getErupt("DevProject"), json));
        DevProject project = transactionTemplate.execute(tx -> eruptDao.lambdaQuery(DevProject.class).eq(DevProject::getCode, "exec-" + mark).one());
        devRepoService.sync(project.getId());
        return project;
    }

    private static JsonObject repo(String alias, Path remote, String target) {
        JsonObject repo = new JsonObject();
        repo.addProperty("alias", alias);
        repo.addProperty("url", remote.toUri().toString());
        repo.addProperty("baseBranch", "develop");
        if (null != target) repo.addProperty("targetBranch", target);
        return repo;
    }

    private DevRequirement requirement(DevProject project, String title) {
        this.as(DevRequirement.class, project.getId());
        JsonObject json = new JsonObject();
        json.addProperty("title", title);
        json.addProperty("description", "Change both repositories.");
        json.addProperty("priority", "MEDIUM");
        JsonArray repos = new JsonArray();
        repos.add("web");
        repos.add("api");
        json.add("repos", repos);
        // the developer, then a QA run of the same executor
        json.addProperty("executor", ClaudeCodeRunner.CODE);
        json.addProperty("qaReview", true);
        transactionTemplate.executeWithoutResult(tx -> eruptModifyService.insertEruptData(EruptCoreService.getErupt("DevRequirement"), json));
        return this.reload(transactionTemplate.execute(tx -> eruptDao.lambdaQuery(DevRequirement.class).eq(DevRequirement::getTitle, title).one()));
    }

    private DevRequirement reload(DevRequirement r) {
        return transactionTemplate.execute(tx -> eruptDao.find(DevRequirement.class, r.getId()));
    }

    private void act(DevRequirement r, String action) {
        devRequirementOperation.exec(List.of(r), null, new String[]{action});
    }

    // ------------------------------------------------------------------ tests

    @Test
    void approvedRequirementRunsAndIsPushed() throws IOException {
        Path web = this.remote("web"), api = this.remote("api");
        DevProject project = this.project(web, api);
        DevRequirement r = this.requirement(project, "change both " + mark);

        this.act(r, "submit");
        this.act(r, "approve");
        r = this.reload(r);
        assertEquals(RequirementStage.QUEUED, r.getStage());
        assertEquals(1, r.getVersion());

        devExecutor.execute(project.getTenantId(), r.getId());
        r = this.reload(r);
        assertEquals(RequirementStage.ACCEPTANCE, r.getStage(), r.getStatusMessage());
        assertEquals("PASS verified over MCP", r.getVerdict(), "the verdict reported over MCP wins over the marker line");
        assertEquals(100, r.getProgress());
        // the executor acts for nobody, so the creator hears the run is done
        assertNotNull(r.getCreator(), "creator recorded");
        String code = r.getCode();
        List<String> notices = transactionTemplate.execute(tx -> eruptDao.getEntityManager()
                .createQuery("select d.noticeLog.title from NoticeLogDetail d where d.noticeLog.noticeScene.code = :scene", String.class)
                .setParameter("scene", DevNoticeListener.SCENE_CODE).getResultList());
        assertTrue(notices.stream().anyMatch(t -> t.contains(code)), notices.toString());
        List<String> bodies = transactionTemplate.execute(tx -> eruptDao.getEntityManager()
                .createQuery("select l.content from NoticeLog l where l.noticeScene.code = :scene", String.class)
                .setParameter("scene", DevNoticeListener.SCENE_CODE).getResultList());
        assertTrue(bodies.stream().allMatch(b -> null != b && !b.isBlank() && !"null".equals(b)), "every notice says something: " + bodies);

        Path workspace = devLayout.workspace(project.getTenantId(), project.getCode(), r.getCode());
        assertEquals("# House rules\nKeep it small.", Files.readString(workspace.resolve("AGENTS.md")));
        assertEquals("@AGENTS.md\n", Files.readString(workspace.resolve("CLAUDE.md")));
        Long id = r.getId();
        List<DevAgentSession> sessions = transactionTemplate.execute(tx -> eruptDao.getEntityManager()
                .createQuery("select s from DevAgentSession s where s.requirement.id = :id order by s.id", DevAgentSession.class)
                .setParameter("id", id).getResultList());
        assertEquals(2, sessions.size());
        assertEquals(SessionStatus.SUCCEEDED, sessions.get(0).getStatus());
        assertEquals("sess-42", sessions.get(0).getSessionId());
        assertEquals(AgentRole.QA, sessions.get(1).getRole());
        List<EventKind> kinds = transactionTemplate.execute(tx -> eruptDao.getEntityManager()
                .createQuery("select e.kind from DevAgentEvent e where e.session.id = :s order by e.seq", EventKind.class)
                .setParameter("s", sessions.get(0).getId()).getResultList());
        // the MCP call lands on another thread, so only the CLI's own lines keep a fixed order
        assertEquals(List.of(EventKind.SYSTEM, EventKind.SYSTEM, EventKind.SYSTEM, EventKind.TEXT, EventKind.TOOL, EventKind.TOOL_RESULT, EventKind.RESULT),
                kinds.stream().sorted().toList());
        assertEquals(EventKind.RESULT, kinds.get(kinds.size() - 1));
        String progress = transactionTemplate.execute(tx -> eruptDao.getEntityManager()
                .createQuery("select e.content from DevAgentEvent e where e.session.id = :s and e.name = 'devReportProgress'", String.class)
                .setParameter("s", sessions.get(0).getId()).getSingleResult());
        assertEquals("55% implement - halfway", progress);
        // the run's token died with it
        assertEquals(0, Files.list(devLayout.root().resolve("run")).count());

        // someone else moves api's develop on meanwhile: delivery rebases onto it
        GitCli cli = eruptGit.getCli();
        Path apiSeed = tmp.resolve("api-seed");
        Files.writeString(apiSeed.resolve("OTHER.md"), "x\n");
        cli.run(apiSeed, "add", "--all");
        cli.run(apiSeed, GitAuth.NONE, IDENT, "commit", "-m", "someone else");
        cli.run(apiSeed, "push", "origin", "develop");

        this.as(DevRequirement.class, project.getId());
        this.act(r, "deliver");
        r = this.reload(r);
        assertEquals(RequirementStage.DELIVERED, r.getStage(), r.getStatusMessage());
        assertEquals("changed by the agent", cli.run(web, "show", "req/req-1:CHANGE.txt"));
        assertEquals("changed by the agent", cli.run(api, "show", "develop:CHANGE.txt"));
        assertEquals("x", cli.run(api, "show", "develop:OTHER.md"));
        assertFalse(Files.exists(devLayout.worktreeDir(project.getTenantId(), project.getCode(), r.getCode(), "web")));
        List<DeliveryStatus> deliveries = transactionTemplate.execute(tx -> eruptDao.getEntityManager()
                .createQuery("select d.status from DevRequirementDelivery d where d.requirement.id = :id", DeliveryStatus.class)
                .setParameter("id", id).getResultList());
        assertEquals(List.of(DeliveryStatus.PUSHED, DeliveryStatus.PUSHED), deliveries);
    }

    @Test
    void theCreatorApprovesOnlyWhenTheProjectAllowsSelfReview() throws IOException {
        DevProject project = this.project(this.remote("web"), this.remote("api"));
        Long pid = project.getId();
        transactionTemplate.executeWithoutResult(tx -> eruptDao.find(DevProject.class, pid).setSelfReview(false));
        DevRequirement r = this.requirement(project, "four eyes " + mark);
        this.act(r, "submit");
        DevRequirement submitted = this.reload(r);
        assertThrows(EruptWebApiRuntimeException.class, () -> this.act(submitted, "approve"), "the creator cannot approve their own");
        assertThrows(EruptWebApiRuntimeException.class, () -> this.act(submitted, "reject"));
        assertEquals(RequirementStage.REVIEW, this.reload(r).getStage());
        transactionTemplate.executeWithoutResult(tx -> eruptDao.find(DevProject.class, pid).setSelfReview(true));
        this.act(submitted, "approve");
        assertEquals(RequirementStage.QUEUED, this.reload(r).getStage());
    }

    @Test
    void aFailedRunCanBeRetriedWithAnInstruction() throws IOException {
        DevProject project = this.project(this.remote("web"), this.remote("api"));
        DevRequirement r = this.requirement(project, "please-fail " + mark);
        this.act(r, "submit");
        this.act(r, "approve");
        devExecutor.execute(project.getTenantId(), r.getId());
        r = this.reload(r);
        assertEquals(RequirementStage.FAILED, r.getStage());
        assertTrue(r.getStatusMessage().contains("cannot do it"), r.getStatusMessage());

        DevRequirement failed = r;
        assertThrows(EruptWebApiRuntimeException.class, () -> this.act(failed, "deliver"), "only an accepted requirement is delivered");
        DevInstructionForm form = new DevInstructionForm();
        form.setInstruction("Try a smaller change.");
        devRequirementOperation.exec(List.of(r), form, new String[]{"retry"});
        r = this.reload(r);
        assertEquals(RequirementStage.QUEUED, r.getStage());
        assertEquals("Try a smaller change.", r.getInstruction());
    }

}
