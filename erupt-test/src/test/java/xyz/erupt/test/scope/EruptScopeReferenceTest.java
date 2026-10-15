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
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.scope.EruptScopeService;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.edit.RefTargetModel;
import xyz.erupt.test.scope.model.TargetScopedTask;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A reference field bound to a scope: the scope holds the id, the model the referenced row.
 */
@Transactional
class EruptScopeReferenceTest extends EruptApplicationTests {

    @Resource
    private EruptScopeService scopeService;

    @Resource
    private EruptService eruptService;

    @Resource
    private EruptModifyService eruptModifyService;

    private RefTargetModel alpha, beta;

    private static EruptModel tasks() {
        return EruptCoreService.getErupt(TargetScopedTask.class.getSimpleName());
    }

    @BeforeEach
    void seed() {
        this.request(null);
        eruptDao.lambdaQuery(TargetScopedTask.class).delete();
        alpha = this.target("alpha");
        beta = this.target("beta");
        for (Object[] row : new Object[][]{{"a1", alpha}, {"a2", alpha}, {"b1", beta}}) {
            TargetScopedTask task = new TargetScopedTask();
            task.setName((String) row[0]);
            task.setTarget((RefTargetModel) row[1]);
            eruptDao.persist(task);
        }
    }

    @AfterEach
    void clear() {
        MetaContext.remove();
        RequestContextHolder.resetRequestAttributes();
    }

    private RefTargetModel target(String name) {
        RefTargetModel target = new RefTargetModel();
        target.setName(name);
        eruptDao.persist(target);
        return target;
    }

    private void request(RefTargetModel target) {
        MetaContext.remove();
        MetaContext.register(new MetaUser(1L, "u1", "u1"));
        MetaContext.register(new MetaErupt(TargetScopedTask.class.getSimpleName()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (null != target) {
            request.addHeader(EruptScopeService.HEADER,
                    URLEncoder.encode("{\"TargetScope\":{\"target\":\"" + target.getId() + "\"}}", StandardCharsets.UTF_8));
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private List<String> names() {
        TableQuery query = new TableQuery();
        query.setPageIndex(1);
        query.setPageSize(50);
        Page page = eruptService.getEruptData(tasks(), query, null);
        return page.getList().stream().map(it -> String.valueOf(it.get("name"))).sorted().toList();
    }

    private TargetScopedTask task(String name) {
        return eruptDao.lambdaQuery(TargetScopedTask.class).eq(TargetScopedTask::getName, name).one();
    }

    @Test
    void noPickShowsEveryRow() {
        assertEquals(List.of("a1", "a2", "b1"), this.names(), "an optional choice left empty is \"all\"");
    }

    @Test
    void pickFiltersByTheReferencedId() {
        this.request(beta);
        assertEquals(List.of("b1"), this.names());
        eruptService.verifyIdPermissions(tasks(), String.valueOf(this.task("b1").getId()));
        assertThrows(EruptWebApiRuntimeException.class, () -> eruptService.verifyIdPermissions(tasks(), String.valueOf(this.task("a1").getId())));
    }

    @Test
    void writesReferTheScopedRow() {
        this.request(beta);
        JsonObject add = new JsonObject();
        add.addProperty("name", "b2");
        eruptModifyService.insertEruptData(tasks(), add);
        assertEquals(beta.getId(), this.task("b2").getTarget().getId(), "a required reference left empty is filled by the scope");

        JsonObject ref = new JsonObject();
        ref.addProperty("id", alpha.getId());
        JsonObject edit = new JsonObject();
        edit.addProperty("id", this.task("b1").getId());
        edit.addProperty("name", "b1");
        edit.add("target", ref);
        eruptModifyService.updateEruptData(tasks(), edit);
        assertEquals(beta.getId(), this.task("b1").getTarget().getId());
    }

    @Test
    void prefillCarriesIdAndLabel() {
        this.request(beta);
        JsonObject data = new JsonObject();
        scopeService.prefill(tasks(), data);
        JsonObject target = data.getAsJsonObject("target");
        assertEquals(beta.getId(), target.get("id").getAsLong());
        assertEquals("beta", target.get("name").getAsString());
    }

}
