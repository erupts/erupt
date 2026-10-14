package xyz.erupt.test.scope;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.controller.EruptBuildController;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.scope.EruptScopeService;
import xyz.erupt.core.scope.EruptScopes;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.scope.model.ProjectScope;
import xyz.erupt.test.scope.model.ScopedTask;
import xyz.erupt.upms.controller.EruptScopeController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A JPA model bound to a scope: the signed pick filters lists, guards ids and fills writes.
 */
@Transactional
class EruptScopeTest extends EruptApplicationTests {

    @Resource
    private EruptScopeService scopeService;

    @Resource
    private EruptService eruptService;

    @Resource
    private EruptModifyService eruptModifyService;

    @Resource
    private EruptBuildController buildController;

    @Resource
    private EruptScopeController scopeController;

    private static EruptModel tasks() {
        return EruptCoreService.getErupt(ScopedTask.class.getSimpleName());
    }

    @BeforeEach
    void seed() {
        this.request(null, 1L);
        eruptDao.lambdaQuery(ScopedTask.class).delete();
        for (String[] row : new String[][]{{"a1", "alpha"}, {"a2", "alpha"}, {"b1", "beta"}}) {
            ScopedTask task = new ScopedTask();
            task.setName(row[0]);
            task.setProject(row[1]);
            eruptDao.persist(task);
        }
    }

    @AfterEach
    void clear() {
        MetaContext.remove();
        RequestContextHolder.resetRequestAttributes();
    }

    // A fresh request: the scope header, if any, and the user behind it
    private void request(String scopeHeader, long uid) {
        MetaContext.remove();
        MetaContext.register(new MetaUser(uid, "u" + uid, "u" + uid));
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (null != scopeHeader) request.addHeader(EruptScopeService.HEADER, scopeHeader);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    // The erupt-scope header a page sends for this pick: plain JSON, URL-encoded
    private static String pick(String project) {
        return URLEncoder.encode("{\"ProjectScope\":{\"project\":\"" + project + "\"}}", StandardCharsets.UTF_8);
    }

    private List<String> names() {
        TableQuery query = new TableQuery();
        query.setPageIndex(1);
        query.setPageSize(50);
        Page page = eruptService.getEruptData(tasks(), query, null);
        return page.getList().stream().map(it -> String.valueOf(it.get("name"))).sorted().toList();
    }

    private Long id(String name) {
        return eruptDao.lambdaQuery(ScopedTask.class).eq(ScopedTask::getName, name).one().getId();
    }

    @Test
    void defaultsAreTheFirstOption() {
        assertEquals("alpha", EruptScopes.get(ProjectScope.class).getProject());
        assertEquals(List.of("a1", "a2"), this.names());
    }

    @Test
    void signedPickFiltersTheList() {
        this.request(pick("beta"), 1L);
        assertEquals("beta", EruptScopes.get(ProjectScope.class).getProject());
        assertEquals(List.of("b1"), this.names());
    }

    @Test
    void optionsAreCheckedOnPick() {
        assertThrows(EruptWebApiRuntimeException.class, () -> scopeService.accept(ProjectScope.class, this.form("gamma")));
        assertEquals("Beta", scopeService.accept(ProjectScope.class, this.form("beta")).labels().get("project"));
    }

    @Test
    void headerValuesOutsideTheOptionsAreIgnored() {
        this.request(pick("gamma"), 1L);
        assertEquals("alpha", EruptScopes.get(ProjectScope.class).getProject(), "a value the user may not pick falls back to the defaults");
        assertEquals(List.of("a1", "a2"), this.names());

        this.request("not json", 1L);
        assertEquals("alpha", EruptScopes.get(ProjectScope.class).getProject(), "a malformed header falls back too");
    }

    @Test
    void idsOutsideTheScopeAreRefused() {
        this.request(pick("beta"), 1L);
        eruptService.verifyIdPermissions(tasks(), String.valueOf(this.id("b1")));
        assertThrows(EruptWebApiRuntimeException.class, () -> eruptService.verifyIdPermissions(tasks(), String.valueOf(this.id("a1"))));
    }

    @Test
    void writesAreForcedIntoTheScope() {
        this.request(pick("beta"), 1L);
        JsonObject add = new JsonObject();
        add.addProperty("name", "b2");
        add.addProperty("project", "alpha");
        eruptModifyService.insertEruptData(tasks(), add);
        assertEquals("beta", eruptDao.lambdaQuery(ScopedTask.class).eq(ScopedTask::getName, "b2").one().getProject());

        JsonObject edit = new JsonObject();
        edit.addProperty("id", this.id("b1"));
        edit.addProperty("name", "b1-renamed");
        edit.addProperty("project", "alpha");
        eruptModifyService.updateEruptData(tasks(), edit);
        assertEquals("beta", eruptDao.lambdaQuery(ScopedTask.class).eq(ScopedTask::getName, "b1-renamed").one().getProject());
    }

    @Test
    void buildAnnouncesTheBinding() throws Exception {
        var scope = buildController.getEruptBuild(ScopedTask.class.getSimpleName()).getScope();
        assertEquals(ProjectScope.class.getSimpleName(), scope.getName());
        assertEquals(List.of("project"), scope.getBind());
        assertTrue(EruptCoreService.isEruptNested(tasks(), ProjectScope.class.getSimpleName()), "the scope form loads under the model's menu");
    }

    @Test
    void headerListsVisibleScopesWithSignedDefaults() {
        EruptScopeController.ScopeView view = scopeController.scopes().stream()
                .filter(it -> it.getName().equals(ProjectScope.class.getSimpleName())).findFirst().orElseThrow();
        assertEquals(ScopedTask.class.getSimpleName(), view.getParent());
        assertEquals(List.of(ScopedTask.class.getSimpleName()), view.getModels(), "shown on the pages of these models only");
        assertEquals(List.of("project"), view.getFields());
        assertEquals(Map.of("project", "Alpha"), view.getDefaults().labels());
    }

    private JsonObject form(String project) {
        JsonObject form = new JsonObject();
        form.addProperty("project", project);
        return form;
    }

}
