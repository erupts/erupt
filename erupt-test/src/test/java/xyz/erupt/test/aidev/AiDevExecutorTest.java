package xyz.erupt.test.aidev;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.erupt.ai_dev.constant.*;
import xyz.erupt.ai_dev.notice.DevNoticeListener;
import xyz.erupt.ai_dev.mcp.DevMcpAuthenticator;
import xyz.erupt.ai_dev.mcp.DevMcpGrants;
import xyz.erupt.ai_dev.mcp.DevTools;
import xyz.erupt.ai.mcp.McpGrant;
import xyz.erupt.ai.core.AiToolboxManager;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.ai.controller.McpController;
import xyz.erupt.ai.vo.mcp.McpCallResult;
import xyz.erupt.ai.vo.mcp.McpRequest;
import xyz.erupt.ai.vo.mcp.McpResult;
import xyz.erupt.ai_dev.runner.ClaudeCodeRunner;
import xyz.erupt.ai_dev.scope.RemoteBranchFetch;
import xyz.erupt.ai_dev.model.DevProjectRepo;
import xyz.erupt.ai_dev.model.*;
import xyz.erupt.ai_dev.prop.EruptAiDevProp;
import xyz.erupt.ai_dev.proxy.DevRequirementOperation;
import xyz.erupt.ai_dev.service.DevExecutor;
import xyz.erupt.ai_dev.service.DevLayout;
import xyz.erupt.ai_dev.service.DevRepoService;
import xyz.erupt.ai_dev.service.DevRunContext;
import xyz.erupt.ai_dev.service.DevWorkspaceService;
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
import xyz.erupt.git.GitRepo;
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
    private RemoteBranchFetch remoteBranchFetch;

    @Resource
    private DevMcpGrants devMcpGrants;

    @Resource
    private DevMcpAuthenticator devMcpAuthenticator;

    @Resource
    private McpController mcpController;

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
    private DevWorkspaceService devWorkspaceService;

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
    void aFailedRunsWorkspaceCanBeThrownAway() throws IOException {
        DevProject project = this.project(this.remote("web"), this.remote("api"));
        DevRequirement r = this.requirement(project, "please-fail and clean " + mark);
        this.act(r, "submit");
        this.act(r, "approve");
        // as a requirement from before creators were kept by id: the run finds it by the creator's name
        Long rid = r.getId();
        transactionTemplate.executeWithoutResult(tx -> eruptDao.find(DevRequirement.class, rid).setCreator(null));
        devExecutor.execute(project.getTenantId(), r.getId());
        r = this.reload(r);
        assertEquals(RequirementStage.FAILED, r.getStage());
        assertNotNull(r.getCreator(), "the creator is found again and kept");
        Long id = r.getId();
        DevRunContext ctx = transactionTemplate.execute(tx -> {
            DevRequirement fresh = eruptDao.find(DevRequirement.class, id);
            return DevRunContext.of(fresh.getProject(), fresh);
        });
        assertTrue(devWorkspaceService.describe(ctx).stream().allMatch(DevWorkspaceService.Workspace::exists));

        this.act(r, "cleanup");
        assertTrue(devWorkspaceService.describe(ctx).stream().noneMatch(DevWorkspaceService.Workspace::exists));
        assertFalse(Files.exists(devLayout.workspace(project.getTenantId(), project.getCode(), r.getCode())));
        GitRepo web = devRepoService.open(project.getTenantId(), project.getCode(), "web");
        assertFalse(web.hasBranch(ctx.workBranch()), "the next run starts from the latest base");
    }

    @Test
    void aRunsTokenCarriesTheCreatorsAiTools() {
        String withCreator = devMcpGrants.issue(UpmsTenant.PLATFORM, -1L, -1L, -1L, AgentRole.DEVELOPER, admin.getId());
        String anonymous = devMcpGrants.issue(UpmsTenant.PLATFORM, -1L, -1L, -1L, AgentRole.DEVELOPER, null);
        try {
            McpGrant grant = devMcpAuthenticator.authenticate(withCreator);
            assertEquals(admin.getId(), grant.uid(), "erupt tools run as the creator");
            assertTrue(grant.tools().containsAll(List.of(DevTools.REQUIREMENT, DevTools.REPORT_PROGRESS)));
            // a super admin may use every exposed tool in AI chat, so the agent may too
            assertTrue(grant.tools().containsAll(AiToolboxManager.getAiMethodMap().keySet()), grant.tools().toString());
            assertFalse(grant.tools().contains(DevTools.REPORT_VERDICT), "the verdict stays the QA run's");
            assertEquals(Set.of(DevTools.REQUIREMENT, DevTools.REPORT_PROGRESS), devMcpAuthenticator.authenticate(anonymous).tools());
            assertNull(devMcpAuthenticator.authenticate("not-a-token"));
        } finally {
            devMcpGrants.revoke(withCreator);
            devMcpGrants.revoke(anonymous);
        }
    }

    @Test
    void anEruptToolCalledOverMcpSeesTheCreatorsSession() {
        String token = devMcpGrants.issue(UpmsTenant.PLATFORM, -1L, -1L, -1L, AgentRole.DEVELOPER, admin.getId());
        MetaContext.remove();
        try {
            MockHttpServletRequest http = new MockHttpServletRequest();
            http.addHeader("Authorization", "Bearer " + token);
            // as over HTTP: the request is bound, and it carries no erupt token header of its own
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(http));
            McpRequest call = new McpRequest();
            call.setId(1);
            call.setMethod("tools/call");
            McpRequest.RequestParams params = new McpRequest.RequestParams();
            params.setName("mcpSessionProbe");
            params.setArguments(Map.of());
            call.setParams(params);
            McpResult result = (McpResult) mcpController.call(http, call).getBody();
            McpCallResult answer = (McpCallResult) result.getResult();
            assertFalse(answer.isError(), answer.getContent().get(0).getText());
            assertEquals(admin.getAccount(), answer.getContent().get(0).getText(), "the tool runs as the creator, logged in");
            // and erupt's data API, powers included, answers as it does for that user's own requests
            params.setName("mcpReadUser");
            params.setArguments(Map.of("id", admin.getId()));
            answer = (McpCallResult) ((McpResult) mcpController.call(http, call).getBody()).getResult();
            assertFalse(answer.isError(), answer.getContent().get(0).getText());
            assertEquals(admin.getAccount(), answer.getContent().get(0).getText());
        } finally {
            RequestContextHolder.resetRequestAttributes();
            devMcpGrants.revoke(token);
            MetaContext.remove();
        }
    }

    @Test
    void structuredToolArgumentsTravelAsJson() {
        String token = devMcpGrants.issue(UpmsTenant.PLATFORM, -1L, -1L, -1L, AgentRole.DEVELOPER, admin.getId());
        MetaContext.remove();
        try {
            MockHttpServletRequest http = new MockHttpServletRequest();
            http.addHeader("Authorization", "Bearer " + token);
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(http));
            McpRequest list = new McpRequest();
            list.setId(1);
            list.setMethod("tools/list");
            // what goes out is erupt's own Gson, nulls included: check that JSON, not the Java objects
            JsonObject listed = JsonParser.parseString(GsonFactory.getGson().toJson(mcpController.call(http, list).getBody())).getAsJsonObject();
            JsonArray tools = listed.getAsJsonObject("result").getAsJsonArray("tools");
            assertFalse(tools.isEmpty());
            for (JsonElement tool : tools) {
                JsonObject schema = tool.getAsJsonObject().getAsJsonObject("inputSchema");
                assertEquals("object", schema.get("type").getAsString(), tool.toString());
                assertValidSchema(schema, tool.getAsJsonObject().get("name").getAsString());
            }
            JsonObject filters = tools.asList().stream().map(JsonElement::getAsJsonObject)
                    .filter(t -> t.get("name").getAsString().equals("mcpEchoFilters")).findFirst().orElseThrow()
                    .getAsJsonObject("inputSchema").getAsJsonObject("properties").getAsJsonObject("filters");
            assertEquals("array", filters.get("type").getAsString(), "a list is declared as an array");
            assertEquals("object", filters.getAsJsonObject("items").get("type").getAsString(), "of objects");

            McpRequest call = new McpRequest();
            call.setId(2);
            call.setMethod("tools/call");
            McpRequest.RequestParams params = new McpRequest.RequestParams();
            params.setName("mcpEchoFilters");
            call.setParams(params);
            params.setArguments(Map.of("filters", List.of(Map.of("field", "name", "value", "erupt"))));
            assertEquals("name=erupt", ((McpCallResult) ((McpResult) mcpController.call(http, call).getBody()).getResult()).getContent().get(0).getText());
            // a client that stringifies structured arguments still gets through
            params.setArguments(Map.of("filters", "[{\"field\":\"account\",\"value\":\"erupt\"}]"));
            McpCallResult lenient = (McpCallResult) ((McpResult) mcpController.call(http, call).getBody()).getResult();
            assertFalse(lenient.isError(), lenient.getContent().get(0).getText());
            assertEquals("account=erupt", lenient.getContent().get(0).getText());
        } finally {
            RequestContextHolder.resetRequestAttributes();
            devMcpGrants.revoke(token);
            MetaContext.remove();
        }
    }

    // what MCP clients (and the Anthropic API behind Claude Code) refuse: a null anywhere, an array without items
    private static void assertValidSchema(JsonElement node, String tool) {
        if (node.isJsonNull()) fail(tool + ": null in its input schema");
        if (node.isJsonArray()) node.getAsJsonArray().forEach(it -> assertValidSchema(it, tool));
        if (!node.isJsonObject()) return;
        JsonObject o = node.getAsJsonObject();
        if (o.has("type") && o.get("type").isJsonPrimitive() && "array".equals(o.get("type").getAsString())) {
            assertTrue(o.has("items") && o.get("items").isJsonObject(), tool + ": an array without items");
        }
        o.entrySet().forEach(e -> assertValidSchema(e.getValue(), tool));
    }

    @Test
    void deletingARequirementTakesItsRunsAndWorkspaceAlong() throws IOException {
        DevProject project = this.project(this.remote("web"), this.remote("api"));
        DevRequirement r = this.requirement(project, "please-fail and delete " + mark);
        this.act(r, "submit");
        this.act(r, "approve");
        devExecutor.execute(project.getTenantId(), r.getId());
        Long id = r.getId();
        assertEquals(RequirementStage.FAILED, this.reload(r).getStage());
        Path workspace = devLayout.workspace(project.getTenantId(), project.getCode(), r.getCode());
        assertTrue(Files.exists(workspace));

        this.as(DevRequirement.class, project.getId());
        transactionTemplate.executeWithoutResult(tx -> eruptModifyService.deleteEruptData(EruptCoreService.getErupt("DevRequirement"), List.of(id), false));
        assertNull(transactionTemplate.execute(tx -> eruptDao.find(DevRequirement.class, id)));
        assertFalse(Files.exists(workspace), "its workspace is gone");
        Long left = transactionTemplate.execute(tx -> eruptDao.getEntityManager()
                .createQuery("select count(s) from DevAgentSession s where s.requirement.id = :id", Long.class).setParameter("id", id).getSingleResult());
        assertEquals(0L, left, "its sessions and their logs go with it");
    }

    @Test
    void branchPickerListsTheRemoteDefaultFirst() throws IOException {
        Path web = this.remote("web");
        Path seed = tmp.resolve("web-seed");
        eruptGit.getCli().run(seed, "push", "origin", "develop:alpha");
        eruptGit.getCli().run(seed, "push", "origin", "develop:release");
        DevProjectRepo repo = new DevProjectRepo();
        repo.setUrl(web.toUri().toString());
        assertEquals(List.of("develop", "alpha", "release"), remoteBranchFetch.completeHandler(repo, "", null));
        assertEquals(List.of("release"), remoteBranchFetch.completeHandler(repo, "REL", null), "matched whatever the case");
        repo.setUrl(tmp.resolve("missing.git").toUri().toString());
        assertEquals(List.of(), remoteBranchFetch.completeHandler(repo, "", null), "an unreachable remote offers nothing");
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
