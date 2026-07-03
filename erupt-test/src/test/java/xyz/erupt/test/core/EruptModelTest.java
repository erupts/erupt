package xyz.erupt.test.core;

import jakarta.annotation.Resource;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.core.proxy.ProxyContext;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.erupt.DataProxyModel;
import xyz.erupt.test.model.erupt.TestDataProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auto-discovers all @Erupt @Entity models under xyz.erupt.model via package scan,
 * validates annotation serialization and CRUD in a unified way without needing to
 * manually add tests for each new model.
 */
@Service
@Rollback
@Transactional
public class EruptModelTest extends EruptApplicationTests {

    @Resource
    private TestDataProxy testDataProxy;

    private static final String MODEL_PACKAGE = "xyz.erupt.test.model";

    // ─── Scan ────────────────────────────────────────────────────────────────

    private List<Class<?>> scanModelClasses() {
        List<Class<?>> list = new ArrayList<>();
        EruptSpringUtil.scannerPackage(new String[]{MODEL_PACKAGE},
                new TypeFilter[]{new AnnotationTypeFilter(Erupt.class)}, list::add);
        return list;
    }

    // ─── Registration & Serialization Validation ─────────────────────────────

    /**
     * Verifies that every @Erupt model is registered in EruptCoreService.
     */
    @Test
    void testAllModelsRegistered() {
        List<Class<?>> models = scanModelClasses();
        assertFalse(models.isEmpty(), "No @Erupt classes found in " + MODEL_PACKAGE);

        for (Class<?> clazz : models) {
            String name = clazz.getSimpleName();
            assertNotNull(EruptCoreService.getErupt(name), name + " must be registered in EruptCoreService");
        }
    }

    /**
     * Verifies all models fully serialize through getEruptView() (triggers clone + field serializable),
     * checks that eruptJson and each field's eruptFieldJson are non-null.
     */
    @Test
    void testEruptViewSerialization() throws Exception {
        List<Class<?>> models = scanModelClasses();
        assertFalse(models.isEmpty(), "No @Erupt classes found in " + MODEL_PACKAGE);

        for (Class<?> clazz : models) {
            String name = clazz.getSimpleName();
            EruptModel view = EruptCoreService.getEruptView(name);

            assertNotNull(view, name + ": getEruptView() must not return null");
            assertNotNull(view.getEruptJson(), name + ": eruptJson must not be null");
            assertTrue(view.getEruptJson().isJsonObject(), name + ": eruptJson must be a JsonObject");
            assertNotNull(view.getEruptFieldModels(), name + ": eruptFieldModels must not be null");

            for (EruptFieldModel fieldModel : view.getEruptFieldModels()) {
                assertNotNull(fieldModel.getFieldName(),
                        name + ": fieldName must not be null");
                assertNotNull(fieldModel.getEruptFieldJson(),
                        name + "." + fieldModel.getFieldName() + ": eruptFieldJson must not be null after serialization");
            }
        }
    }

    /**
     * Every @Erupt model must declare at least one @EruptField.
     */
    @Test
    void testEruptFieldPresence() {
        for (Class<?> clazz : scanModelClasses()) {
            long count = Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(EruptField.class))
                    .count();
            assertTrue(count > 0, clazz.getSimpleName() + " must declare at least one @EruptField");
        }
    }

    /**
     * @Search.operator AUTO must resolve per owning edit type (RANGE for DATE, EQ otherwise)
     * and must not leak across value-equal @Search annotations from other fields
     * (annotation equals() is value-based, so a shared proxy cache would mix operators up).
     */
    @Test
    void testSearchOperatorResolution() {
        // DATE field: AUTO resolves to RANGE (SearchProxy reads the owning field from ProxyContext)
        EruptModel dateModel = EruptCoreService.getErupt("DateModel");
        ProxyContext.set(dateModel.getEruptFieldMap().get("date").getField(), false);
        assertEquals(QueryExpression.RANGE, dateModel.getEruptFieldMap().get("date").getEruptField().edit().search().operator());
        // plain NUMBER field: AUTO resolves to EQ
        EruptModel numberModel = EruptCoreService.getErupt("NumberModel");
        ProxyContext.set(numberModel.getEruptFieldMap().get("percent").getField(), false);
        assertEquals(QueryExpression.EQ, numberModel.getEruptFieldMap().get("percent").getEruptField().edit().search().operator());
        // explicitly configured operator wins over AUTO resolution
        EruptModel inputModel = EruptCoreService.getErupt("InputModel");
        assertEquals(QueryExpression.LIKE, inputModel.getEruptFieldMap().get("keyword").getEruptField().edit().search().operator());
        // the serialized JSON handed to the frontend carries the resolved operator
        EruptFieldModel dateFieldView = EruptCoreService.getEruptView("DateModel").getEruptFieldModels().stream()
                .filter(f -> "date".equals(f.getFieldName())).findFirst().orElseThrow();
        assertEquals(QueryExpression.RANGE.name(), dateFieldView.getEruptFieldJson()
                .getAsJsonObject("edit").getAsJsonObject("search").get("operator").getAsString());
    }

    // ─── Unified CRUD Validation ──────────────────────────────────────────────

    /**
     * Executes persist → find validation against all scanned models.
     */
    @Test
    void testAllEditTypesCrud() throws Exception {
        List<Class<?>> models = scanModelClasses();
        assertFalse(models.isEmpty(), "No @Erupt @Entity classes found in " + MODEL_PACKAGE);

        for (Class<?> clazz : models) {
            BaseModel entity = buildEntity(clazz);
            eruptDao.persist(entity);
            assertNotNull(entity.getId(), clazz.getSimpleName() + ": id must be set after persist");

            Object found = eruptDao.getEntityManager().find(clazz, entity.getId());
            assertNotNull(found, clazz.getSimpleName() + ": must be findable after persist");
        }
    }

    /**
     * Validates update for all models: updates the first String field via JPA dirty-checking,
     * then flush + clear and re-find to confirm the change is persisted.
     */
    @Test
    void testAllEditTypesMerge() throws Exception {
        for (Class<?> clazz : scanModelClasses()) {
            Field stringField = findFirstStringField(clazz);
            if (stringField == null) continue;

            BaseModel entity = buildEntity(clazz);
            eruptDao.persist(entity);
            Long id = entity.getId();

            stringField.setAccessible(true);
            stringField.set(entity, "updated");
            eruptDao.getEntityManager().flush();
            eruptDao.getEntityManager().clear();

            Object found = eruptDao.getEntityManager().find(clazz, id);
            assertNotNull(found, clazz.getSimpleName() + ": must be findable after update");
            assertEquals("updated", stringField.get(found),
                    clazz.getSimpleName() + "." + stringField.getName() + ": value must reflect update");
        }
    }

    /**
     * Validates explicit merge for all models: persist → flush → detach → modify field → eruptDao.merge() → clear → find.
     */
    @Test
    void testAllEditTypesExplicitMerge() throws Exception {
        for (Class<?> clazz : scanModelClasses()) {
            Field stringField = findFirstStringField(clazz);
            if (stringField == null) continue;

            BaseModel entity = buildEntity(clazz);
            eruptDao.persist(entity);
            eruptDao.getEntityManager().flush();
            Long id = entity.getId();

            eruptDao.getEntityManager().detach(entity);
            stringField.setAccessible(true);
            stringField.set(entity, "explicit-merge");
            eruptDao.merge(entity);
            eruptDao.getEntityManager().flush();
            eruptDao.getEntityManager().clear();

            Object found = eruptDao.getEntityManager().find(clazz, id);
            assertNotNull(found, clazz.getSimpleName() + ": must be findable after explicit merge");
            assertEquals("explicit-merge", stringField.get(found),
                    clazz.getSimpleName() + "." + stringField.getName() + ": value must reflect explicit merge");
        }
    }

    /**
     * Validates delete for all models: persist → flush → remove → flush → find returns null.
     * @OneToMany / @ManyToMany are managed via join table; Hibernate automatically cleans up association rows.
     */
    @Test
    void testAllEditTypesDelete() throws Exception {
        for (Class<?> clazz : scanModelClasses()) {
            BaseModel entity = buildEntity(clazz);
            eruptDao.persist(entity);
            eruptDao.getEntityManager().flush();
            Long id = entity.getId();

            eruptDao.getEntityManager().remove(entity);
            eruptDao.getEntityManager().flush();

            assertNull(eruptDao.getEntityManager().find(clazz, id),
                    clazz.getSimpleName() + ": must not be findable after delete");
        }
    }

    /**
     * Executes lambdaQuery().list() on all models: queries after persist, verifies the result
     * is non-null and contains the persisted entity.
     */
    @Test
    @SuppressWarnings("unchecked")
    void testAllEditTypesLambdaQuery() throws Exception {
        for (Class<?> clazz : scanModelClasses()) {
            BaseModel entity = buildEntity(clazz);
            eruptDao.persist(entity);
            eruptDao.getEntityManager().flush();
            Long id = entity.getId();

            List<BaseModel> result = eruptDao.lambdaQuery((Class<BaseModel>) clazz).list();
            assertNotNull(result, clazz.getSimpleName() + ": lambdaQuery result must not be null");
            assertTrue(result.stream().anyMatch(e -> id.equals(e.getId())),
                    clazz.getSimpleName() + ": lambdaQuery result must contain persisted entity");
        }
    }

    // ─── DataProxy Lifecycle Validation ──────────────────────────────────────

    /**
     * beforeAdd sets default status and creator before add.
     */
    @Test
    void testDataProxyBeforeAdd() {
        DataProxyModel model = new DataProxyModel();
        model.setTitle("test");
        testDataProxy.beforeAdd(model);
        assertEquals("DRAFT", model.getStatus(), "beforeAdd must set status to DRAFT");
        assertEquals("system", model.getCreator(), "beforeAdd must set creator to system");
    }

    /**
     * validate throws EruptException when title is blank.
     */
    @Test
    void testDataProxyValidateThrowsOnBlankTitle() {
        DataProxyModel blank = new DataProxyModel();
        blank.setTitle("  ");
        assertThrows(EruptException.class, () -> testDataProxy.validate(blank));

        DataProxyModel nullTitle = new DataProxyModel();
        assertThrows(EruptException.class, () -> testDataProxy.validate(nullTitle));
    }

    /**
     * beforeDelete rejects deletion of published records and allows deletion of draft records.
     */
    @Test
    void testDataProxyBeforeDelete() {
        DataProxyModel published = new DataProxyModel();
        published.setStatus("PUBLISHED");
        assertThrows(EruptException.class, () -> testDataProxy.beforeDelete(published),
                "beforeDelete must throw for PUBLISHED records");

        DataProxyModel draft = new DataProxyModel();
        draft.setStatus("DRAFT");
        assertDoesNotThrow(() -> testDataProxy.beforeDelete(draft),
                "beforeDelete must not throw for non-PUBLISHED records");
    }

    // ─── Reflection Helpers ───────────────────────────────────────────────────

    private BaseModel buildEntity(Class<?> clazz) throws Exception {
        BaseModel entity = (BaseModel) clazz.getDeclaredConstructor().newInstance();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (field.isAnnotationPresent(jakarta.persistence.Transient.class)) continue;
            field.setAccessible(true);

            if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
                BaseModel ref = buildEntity(field.getType());
                eruptDao.persist(ref);
                field.set(entity, ref);
            } else if (field.isAnnotationPresent(ManyToMany.class) || field.isAnnotationPresent(OneToMany.class)) {
                Class<?> refType = resolveCollectionGeneric(field);
                BaseModel ref = buildEntity(refType);
                eruptDao.persist(ref);
                field.set(entity, new ArrayList<>(List.of(ref)));
            } else {
                setScalarValue(entity, field);
            }
        }
        return entity;
    }

    private Class<?> resolveCollectionGeneric(Field field) {
        return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

    private Field findFirstStringField(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .filter(f -> !f.isAnnotationPresent(jakarta.persistence.Transient.class))
                .filter(f -> f.getType() == String.class)
                .findFirst()
                .orElse(null);
    }

    private void setScalarValue(Object target, Field field) throws Exception {
        Class<?> t = field.getType();
        if (t == String.class) field.set(target, "test");
        else if (t == Integer.class || t == int.class) field.set(target, 1);
        else if (t == Long.class || t == long.class) field.set(target, 1L);
        else if (t == Boolean.class || t == boolean.class) field.set(target, true);
        else if (t == Double.class || t == double.class) field.set(target, 1.0d);
        else if (t == Float.class || t == float.class) field.set(target, 1.0f);
        else if (t == Date.class) field.set(target, new Date());
        else if (t == LocalDateTime.class) field.set(target, LocalDateTime.now());
        else if (t == BigDecimal.class) field.set(target, BigDecimal.ONE);
    }
}
