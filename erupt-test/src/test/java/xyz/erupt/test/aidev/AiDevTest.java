package xyz.erupt.test.aidev;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.erupt.ai_dev.constant.MemberRole;
import xyz.erupt.core.service.PreEruptDataService;
import xyz.erupt.ai_dev.constant.RepoStatus;
import xyz.erupt.ai_dev.constant.RequirementStage;
import xyz.erupt.ai_dev.model.DevProject;
import xyz.erupt.ai_dev.model.DevProjectMember;
import xyz.erupt.ai_dev.model.DevProjectRepo;
import xyz.erupt.ai_dev.model.DevRequirement;
import xyz.erupt.ai_dev.service.DevLayout;
import xyz.erupt.ai_dev.service.DevRepoService;
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
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.git.EruptGit;
import xyz.erupt.git.GitAuth;
import xyz.erupt.git.GitCli;
import xyz.erupt.git.GitException;
import xyz.erupt.git.GitProp;
import xyz.erupt.git.GitRepo;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * erupt-ai-dev end to end on H2: projects stay private to their members, requirements live in the
 * project picked in the page header, repositories are cloned on the server and worktrees are read back
 * from git.
 */
class AiDevTest extends EruptApplicationTests {

    @Resource
    private PreEruptDataService preEruptDataService;

    @Resource
    private EruptModifyService eruptModifyService;

    @Resource
    private EruptService eruptService;

    @Resource
    private EruptSessionService eruptSessionService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private DevRepoService devRepoService;

    @Resource
    private DevLayout devLayout;

    @Resource
    private DevWorkspaceService devWorkspaceService;

    @Resource
    private EruptGit eruptGit;

    @Resource
    private GitProp gitProp;

    // unique per run: the H2 database outlives a test method
    private final String mark = UUID.randomUUID().toString().substring(0, 6);

    private final List<Object> created = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        gitProp.setAllowLocalRemote(false);
        MetaContext.remove();
        RequestContextHolder.resetRequestAttributes();
        transactionTemplate.executeWithoutResult(tx -> {
            eruptDao.getEntityManager().createQuery("delete from DevRequirement r where r.project.code like :c")
                    .setParameter("c", "%" + mark).executeUpdate();
            eruptDao.lambdaQuery(DevProject.class).like(DevProject::getCode, mark).list().forEach(eruptDao::delete);
            for (Object it : created) {
                Object managed = eruptDao.getEntityManager().merge(it);
                eruptDao.delete(managed);
            }
        });
    }

    // ------------------------------------------------------------------ fixtures

    private EruptUser user(String name, boolean admin) {
        return transactionTemplate.execute(tx -> {
            EruptUser user = new EruptUser();
            user.setAccount(name + "-" + mark);
            user.setName(name);
            user.setIsAdmin(admin);
            user.setStatus(true);
            eruptDao.persist(user);
            created.add(user);
            return user;
        });
    }

    // Acts as the user on the erupt model, with the project picked in the header
    private void as(EruptUser user, Class<?> erupt, Long project) {
        String token = "ai-dev-" + mark + "-" + user.getId();
        MetaUserinfo info = new MetaUserinfo();
        info.setId(user.getId());
        info.setAccount(user.getAccount());
        info.setUsername(user.getName());
        info.setSuperAdmin(Boolean.TRUE.equals(user.getIsAdmin()));
        eruptSessionService.put(SessionKey.USER_INFO + token, GsonFactory.getGson().toJson(info), 10, TimeUnit.MINUTES);
        // the power handler reads the menus a login grants; none are needed here
        eruptSessionService.putMap(SessionKey.MENU_VALUE_MAP + token, Map.of("ai-dev-test", "{}"), 10, TimeUnit.MINUTES);
        MetaContext.remove();
        MetaContext.register(new MetaUser(user.getId(), user.getAccount(), user.getName()));
        MetaContext.register(new MetaErupt(erupt.getSimpleName()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN, token);
        if (null != project) {
            request.addHeader(EruptScopeService.HEADER,
                    URLEncoder.encode("{\"DevProjectScope\":{\"project\":\"" + project + "\"}}", StandardCharsets.UTF_8));
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private static EruptModel model(Class<?> erupt) {
        return EruptCoreService.getErupt(erupt.getSimpleName());
    }

    private JsonObject projectJson(String code, String... repos) {
        JsonObject json = new JsonObject();
        json.addProperty("code", code);
        json.addProperty("name", "Project " + code);
        JsonArray list = new JsonArray();
        for (int i = 0; i < repos.length; i += 2) {
            JsonObject repo = new JsonObject();
            repo.addProperty("alias", repos[i]);
            repo.addProperty("url", repos[i + 1]);
            repo.addProperty("baseBranch", "develop");
            list.add(repo);
        }
        json.add("repos", list);
        // the form always submits every tab, empty or not
        json.add("members", new JsonArray());
        return json;
    }

    private DevProject addProject(JsonObject json) {
        transactionTemplate.executeWithoutResult(tx -> eruptModifyService.insertEruptData(model(DevProject.class), json));
        return transactionTemplate.execute(tx -> {
            DevProject project = eruptDao.lambdaQuery(DevProject.class).eq(DevProject::getCode, json.get("code").getAsString()).one();
            project.getMembers().forEach(it -> it.getUser().getName());
            return project;
        });
    }

    private DevRequirement addRequirement(String title, String... repos) {
        JsonObject json = new JsonObject();
        json.addProperty("title", title);
        json.addProperty("priority", "HIGH");
        JsonArray list = new JsonArray();
        Arrays.stream(repos).forEach(list::add);
        json.add("repos", list);
        transactionTemplate.executeWithoutResult(tx -> eruptModifyService.insertEruptData(model(DevRequirement.class), json));
        return transactionTemplate.execute(tx -> eruptDao.lambdaQuery(DevRequirement.class).eq(DevRequirement::getTitle, title).one());
    }

    private List<Map<String, Object>> list(Class<?> erupt) {
        TableQuery query = new TableQuery();
        query.setPageIndex(1);
        query.setPageSize(50);
        return transactionTemplate.execute(tx -> eruptService.getEruptData(model(erupt), query, null).getList().stream().toList());
    }

    // ------------------------------------------------------------------ tests

    @Test
    void creatorOwnsTheProjectAndOnlyMembersSeeIt() {
        EruptUser alice = this.user("alice", false), bob = this.user("bob", false);
        this.as(alice, DevProject.class, null);
        DevProject project = this.addProject(this.projectJson("web-" + mark, "web", "https://github.com/acme/web.git"));

        DevProjectMember owner = project.getMembers().iterator().next();
        assertEquals(alice.getId(), owner.getUser().getId());
        assertEquals(MemberRole.OWNER, owner.getRole());
        assertEquals(1, this.list(DevProject.class).stream().filter(it -> project.getId().equals(it.get("id"))).count());

        this.as(bob, DevProject.class, null);
        assertTrue(this.list(DevProject.class).stream().noneMatch(it -> project.getId().equals(it.get("id"))), "not a member");
        // nor by id, which skips the list filter
        assertThrows(EruptWebApiRuntimeException.class,
                () -> preEruptDataService.getEruptData(model(DevProject.class), project.getId().toString(), false));
        JsonObject edit = this.projectJson(project.getCode(), "web", "https://github.com/acme/web.git");
        edit.addProperty("id", project.getId());
        assertThrows(EruptWebApiRuntimeException.class,
                () -> transactionTemplate.executeWithoutResult(tx -> eruptModifyService.updateEruptData(model(DevProject.class), edit)));
    }

    @Test
    void projectInputIsChecked() {
        EruptUser alice = this.user("alice", false);
        this.as(alice, DevProject.class, null);
        for (JsonObject bad : List.of(
                this.projectJson("Web App"),
                this.projectJson("dup-" + mark, "web", "https://github.com/acme/a.git", "web", "https://github.com/acme/b.git"),
                this.projectJson("cred-" + mark, "web", "https://user:token@github.com/acme/web.git"),
                this.projectJson("ext-" + mark, "web", "ext::sh -c id"),
                this.projectJson("alias-" + mark, "../x", "https://github.com/acme/web.git"))) {
            assertThrows(RuntimeException.class,
                    () -> transactionTemplate.executeWithoutResult(tx -> eruptModifyService.insertEruptData(model(DevProject.class), bad)),
                    bad.toString());
        }
    }

    @Test
    void requirementsAreNumberedInsideTheScopedProject() {
        EruptUser alice = this.user("alice", false), bob = this.user("bob", false);
        this.as(alice, DevProject.class, null);
        DevProject project = this.addProject(this.projectJson("app-" + mark,
                "web", "https://github.com/acme/web.git", "api", "https://github.com/acme/api.git"));

        this.as(alice, DevRequirement.class, project.getId());
        DevRequirement first = this.addRequirement("first " + mark, "web", "api");
        DevRequirement second = this.addRequirement("second " + mark, "api");
        assertEquals("REQ-1", first.getCode());
        assertEquals("REQ-2", second.getCode());
        assertEquals(project.getId(), first.getProject().getId(), "bound by the page-header scope");
        assertEquals(RequirementStage.DRAFT, first.getStage());
        assertEquals(Set.of("web", "api"), first.getRepos());
        assertEquals(2, this.list(DevRequirement.class).size());
        assertThrows(RuntimeException.class, () -> this.addRequirement("third " + mark, "mobile"), "not a repository of the project");

        // a project the user is not a member of cannot be picked in the header
        this.as(bob, DevRequirement.class, project.getId());
        assertThrows(EruptWebApiRuntimeException.class, () -> this.list(DevRequirement.class));
    }

    @Test
    void repositoriesAreClonedAndLeftoverWorkspacesRemoved() throws IOException {
        GitCli cli = eruptGit.getCli();
        try {
            eruptGit.version();
        } catch (GitException e) {
            Assumptions.abort("git is not installed");
        }
        gitProp.setAllowLocalRemote(true);
        Path tmp = Files.createTempDirectory("ai-dev-remote");
        Path remote = tmp.resolve("web.git"), seed = tmp.resolve("seed");
        cli.run(null, "init", "--bare", "--initial-branch=develop", remote.toString());
        cli.run(null, "clone", remote.toUri().toString(), seed.toString());
        cli.run(seed, "checkout", "-b", "develop");
        Files.writeString(seed.resolve("README.md"), "hello\n");
        cli.run(seed, "add", "--all");
        cli.run(seed, GitAuth.NONE, Map.of("GIT_AUTHOR_NAME", "t", "GIT_AUTHOR_EMAIL", "t@t", "GIT_COMMITTER_NAME", "t", "GIT_COMMITTER_EMAIL", "t@t"), "commit", "-m", "init");
        cli.run(seed, "push", "origin", "develop");

        EruptUser admin = this.user("root", true);
        this.as(admin, DevProject.class, null);
        DevProject project = this.addProject(this.projectJson("git-" + mark, "web", remote.toUri().toString()));
        devRepoService.sync(project.getId());

        DevProjectRepo repo = transactionTemplate.execute(tx -> eruptDao.find(DevProject.class, project.getId()).getRepos().iterator().next());
        assertEquals(RepoStatus.READY, repo.getStatus(), repo.getStatusMessage());
        Path bare = devLayout.repoDir(project.getTenantId(), project.getCode(), "web");
        assertTrue(Files.isDirectory(bare));

        GitRepo gitRepo = eruptGit.open(bare);
        Path wt = devLayout.worktreeDir(project.getTenantId(), project.getCode(), "REQ-1", "web");
        gitRepo.addWorktree(wt, "req/req-1", "origin/develop");
        // a worktree outside the project's directory is not the project's
        gitRepo.addWorktree(tmp.resolve("elsewhere"), "other", "origin/develop");

        // the project counts the workspace of REQ-1, not a worktree outside its directory
        assertEquals(List.of("req-1"), devWorkspaceService.workspaces(project.getTenantId(), project.getCode()));

        // a workspace of a requirement that no longer exists is removed, and git forgets its worktree
        devWorkspaceService.discardDirectory(project.getTenantId(), project.getCode(), "req-1");
        assertFalse(Files.exists(wt));
        assertTrue(devWorkspaceService.workspaces(project.getTenantId(), project.getCode()).isEmpty());
        assertTrue(gitRepo.worktrees().stream().noneMatch(it -> it.path().endsWith(Path.of("req-1", "web"))), "pruned from the repository");
        assertThrows(IOException.class, () -> devWorkspaceService.discardDirectory(project.getTenantId(), project.getCode(), "../escape"),
                "only a workspace of the project itself");
    }

}
