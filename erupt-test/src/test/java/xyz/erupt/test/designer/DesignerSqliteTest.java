package xyz.erupt.test.designer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.designer.model.DesignerEntity;
import xyz.erupt.designer.pojo.DesignerForm;
import xyz.erupt.designer.service.DesignerDataService;
import xyz.erupt.designer.service.EruptDesignerService;
import xyz.erupt.designer.store.DesignerStore;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.test.EruptApplicationTests;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Designer data on the embedded SQLite store: publish → real table, CRUD, pushed-down
 * search on scalar / reference / multi-value fields and additive schema evolution.
 */
@Rollback
@Transactional
public class DesignerSqliteTest extends EruptApplicationTests {

    private static final String CLASS_NAME = "DsSqliteTest";

    private static final String FORM = """
            {"erupt":{"name":"Sqlite Test"},"fields":[
             {"fieldName":"name","view":{"title":"Name"},"edit":{"title":"Name","type":"INPUT"}},
             {"fieldName":"qty","fieldType":"Integer","view":{"title":"Qty"},"edit":{"title":"Qty","type":"NUMBER"}},
             {"fieldName":"active","view":{"title":"Active"},"edit":{"title":"Active","type":"BOOLEAN"}},
             {"fieldName":"happenAt","view":{"title":"At"},"edit":{"title":"At","type":"DATE"}},
             {"fieldName":"owner","linkErupt":"EruptUser","view":{"title":"Owner","column":"name"},"edit":{"title":"Owner","type":"REFERENCE_TABLE"}},
             {"fieldName":"tags","view":{"title":"Tags"},"edit":{"title":"Tags","type":"MULTI_CHOICE"}}
            ]}""";

    private final Gson gson = GsonFactory.getGson();

    @Resource
    private EruptDesignerService designerService;

    @Resource
    private DesignerStore store;

    @Resource
    private EruptModifyService modifyService;

    @BeforeEach
    void setUp() {
        this.cleanUp();
        DesignerEntity entity = new DesignerEntity();
        entity.setClassName(CLASS_NAME);
        entity.setName(CLASS_NAME);
        entity.setUpdateTime(new Date());
        eruptDao.persistAndFlush(entity);
    }

    @AfterEach
    void cleanUp() {
        EruptCoreService.unregisterErupt(CLASS_NAME);
        store.dropTable(CLASS_NAME);
        eruptDao.lambdaQuery(DesignerEntity.class).eq(DesignerEntity::getClassName, CLASS_NAME).delete();
    }

    @Test
    void publishCrudSearchAndEvolve() {
        designerService.publish(CLASS_NAME, gson.fromJson(FORM, DesignerForm.class));
        EruptModel model = EruptCoreService.getErupt(CLASS_NAME);
        assertNotNull(model);
        IEruptDataService service = DataProcessorManager.getEruptDataProcessor(model.getClazz());
        assertInstanceOf(DesignerDataService.class, service);

        // insert with generated key
        Object a = gson.fromJson("""
                {"name":"alpha","qty":3,"active":true,"happenAt":"2026-09-09T10:00:00.000",
                 "owner":{"id":1,"label":"admin","name":"admin"},"tags":["x","y"]}""", model.getClazz());
        Object b = gson.fromJson("""
                {"name":"beta","qty":7,"active":false,"owner":{"id":2,"label":"guest"},"tags":["z"]}""", model.getClazz());
        service.addData(model, a);
        service.addData(model, b);
        Long idA = ((BaseModel) a).getId();
        assertNotNull(idA);
        assertNotEquals(idA, ((BaseModel) b).getId());

        assertEquals(2, this.query(service, model, null, null).getTotal());

        // scalar, reference (by id) and multi-value (by element) search all run in SQL
        assertEquals(1, this.query(service, model, new Condition("name", "alph", QueryExpression.LIKE), null).getTotal());
        assertEquals(1, this.query(service, model, new Condition("owner", "1", QueryExpression.EQ), null).getTotal());
        assertEquals(0, this.query(service, model, new Condition("owner", List.of("1", "2"), QueryExpression.NOT_IN), null).getTotal());
        assertEquals(1, this.query(service, model, new Condition("tags", "y", QueryExpression.EQ), null).getTotal());
        assertEquals(2, this.query(service, model, new Condition("tags", List.of("y", "z"), QueryExpression.IN), null).getTotal());
        assertEquals(2, this.query(service, model, new Condition("qty", List.of("2", "9"), QueryExpression.RANGE), null).getTotal());
        assertEquals(1, this.query(service, model, new Condition("active", true, QueryExpression.EQ), null).getTotal());

        // list rows: typed values, reference flattened to owner_label, sorted in SQL
        Page page = this.query(service, model, null, new Sort("qty", Direction.DESC));
        Map<String, Object> first = page.getList().iterator().next();
        assertEquals("beta", first.get("name"));
        assertEquals("guest", first.get("owner_label"));
        assertEquals(Boolean.FALSE, first.get("active"));
        Map<String, Object> alpha = page.getList().stream().filter(it -> "alpha".equals(it.get("name"))).findFirst().orElseThrow();
        assertInstanceOf(Date.class, alpha.get("happenAt"));
        assertEquals("admin", alpha.get("owner_name"));
        assertDoesNotThrow(() -> this.query(service, model, null, new Sort("owner", Direction.ASC)));

        // read back as bean: JSON columns restored to Map / Set, integer column to Boolean
        Object loaded = service.findDataById(model, idA);
        assertEquals(1L, ((Map<?, ?>) this.field(loaded, "owner")).get("id"));
        assertEquals(Set.of("x", "y"), new HashSet<>((Collection<?>) this.field(loaded, "tags")));
        assertEquals(Boolean.TRUE, this.field(loaded, "active"));
        assertInstanceOf(Date.class, this.field(loaded, "happenAt"));

        // update / delete
        this.setField(loaded, "name", "alpha2");
        service.editData(model, loaded);
        assertEquals("alpha2", this.field(service.findDataById(model, idA), "name"));
        service.deleteData(model, loaded);
        assertNull(service.findDataById(model, idA));
        assertEquals(1, this.query(service, model, null, null).getTotal());

        // re-publish with an extra field: column added, existing rows kept
        DesignerForm evolved = gson.fromJson(FORM, DesignerForm.class);
        evolved.getFields().add(gson.fromJson(
                "{\"fieldName\":\"remark\",\"view\":{\"title\":\"Remark\"},\"edit\":{\"title\":\"Remark\",\"type\":\"TEXTAREA\"}}",
                DesignerForm.DesignerField.class));
        designerService.publish(CLASS_NAME, evolved);
        EruptModel model2 = EruptCoreService.getErupt(CLASS_NAME);
        assertNotSame(model, model2);
        assertEquals(1, this.query(service, model2, null, null).getTotal());
        Object c = gson.fromJson("{\"name\":\"gamma\",\"remark\":\"added later\"}", model2.getClazz());
        service.addData(model2, c);
        assertEquals("added later", this.field(service.findDataById(model2, ((BaseModel) c).getId()), "remark"));
    }

    /**
     * Cell editing against the SQLite store: the dynamically generated carrier class and the
     * JSON-backed reference / multi-value columns must survive a single-field update.
     */
    @Test
    void updatesSingleCell() {
        designerService.publish(CLASS_NAME, gson.fromJson(FORM, DesignerForm.class));
        EruptModel model = EruptCoreService.getErupt(CLASS_NAME);
        IEruptDataService service = DataProcessorManager.getEruptDataProcessor(model.getClazz());
        Object row = gson.fromJson("""
                {"name":"alpha","qty":3,"active":true,"owner":{"id":1,"label":"admin"},"tags":["x"]}""", model.getClazz());
        service.addData(model, row);
        String id = String.valueOf(((BaseModel) row).getId());

        MetaContext.register(new MetaUser(1L, "admin", "admin"));
        try {
            modifyService.updateEruptCell(model, id, "name", new JsonPrimitive("alpha-cell"));
            modifyService.updateEruptCell(model, id, "tags", gson.toJsonTree(List.of("x", "y")));
            modifyService.updateEruptCell(model, id, "owner",
                    gson.fromJson("{\"id\":2,\"label\":\"guest\"}", JsonElement.class));
        } finally {
            MetaContext.remove();
        }

        Object loaded = service.findDataById(model, ((BaseModel) row).getId());
        assertEquals("alpha-cell", this.field(loaded, "name"));
        assertEquals(Set.of("x", "y"), new HashSet<>((Collection<?>) this.field(loaded, "tags")));
        assertEquals("guest", ((Map<?, ?>) this.field(loaded, "owner")).get("label"));
        // fields the patches never touched keep their values
        assertEquals(3, ((Number) this.field(loaded, "qty")).intValue());
        assertEquals(Boolean.TRUE, this.field(loaded, "active"));
    }

    @Test
    void rejectsUnsafeFieldNames() {
        DesignerForm form = gson.fromJson(FORM, DesignerForm.class);
        form.getFields().get(0).setFieldName("order");
        assertThrows(RuntimeException.class, () -> designerService.publish(CLASS_NAME, form));
        form.getFields().get(0).setFieldName("na me");
        assertThrows(RuntimeException.class, () -> designerService.publish(CLASS_NAME, form));
        // nothing registered when storage preparation fails
        assertNull(EruptCoreService.getErupt(CLASS_NAME));
    }

    private Page query(IEruptDataService service, EruptModel model, Condition condition, Sort sort) {
        Page page = new Page(1, 10);
        if (null != sort) page.setSort(List.of(sort));
        return service.queryList(model, page, EruptQuery.builder()
                .conditions(null == condition ? new ArrayList<>() : List.of(condition)).build());
    }

    private Object field(Object bean, String name) {
        try {
            java.lang.reflect.Field f = bean.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(bean);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void setField(Object bean, String name, Object value) {
        try {
            java.lang.reflect.Field f = bean.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(bean, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

}
