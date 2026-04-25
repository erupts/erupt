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
import xyz.erupt.annotation.exception.EruptException;
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
 * 通过包扫描自动发现 xyz.erupt.model 下所有 @Erupt @Entity 模型，
 * 统一验证注解序列化正确性 + CRUD 可用性，无需每加一个 Model 就手动添加测试。
 */
@Service
@Rollback
@Transactional
public class EruptModelTest extends EruptApplicationTests {

    @Resource
    private TestDataProxy testDataProxy;

    private static final String MODEL_PACKAGE = "xyz.erupt.test.model";

    // ─── 扫描 ─────────────────────────────────────────────────────────────────

    private List<Class<?>> scanModelClasses() {
        List<Class<?>> list = new ArrayList<>();
        EruptSpringUtil.scannerPackage(new String[]{MODEL_PACKAGE},
                new TypeFilter[]{new AnnotationTypeFilter(Erupt.class)}, list::add);
        return list;
    }

    // ─── 注册 & 序列化校验 ────────────────────────────────────────────────────

    /**
     * 验证每个 @Erupt 模型均已注册到 EruptCoreService。
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
     * 验证所有模型通过 getEruptView() 完整序列化（触发 clone + field serializable），
     * 校验 eruptJson 和每个字段的 eruptFieldJson 均不为空。
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
     * 每个 @Erupt 模型必须声明至少一个 @EruptField 字段。
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

    // ─── CRUD 统一校验 ────────────────────────────────────────────────────────

    /**
     * 对所有扫描到的模型执行 persist → find 验证。
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
     * 对所有模型验证 update：通过 JPA 脏检查更新第一个 String 字段，
     * flush + clear 后重新 find，验证变更已持久化。
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
     * 对所有模型验证显式 merge：persist → flush → detach → 修改字段 → eruptDao.merge() → clear → find。
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
     * 对所有模型验证 delete：persist → flush → remove → flush → find 返回 null。
     * @OneToMany / @ManyToMany 均通过 join table 管理，Hibernate 会自动清理关联行。
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
     * 对所有模型执行 lambdaQuery().list()：persist 后查询，验证结果非 null 且包含已持久化的实体。
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

    // ─── DataProxy 生命周期校验 ───────────────────────────────────────────────

    /**
     * beforeAdd 在新增前设置默认 status 和 creator。
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
     * validate 在 title 为空白时抛出 EruptException。
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
     * beforeDelete 拒绝删除已发布记录，允许删除草稿记录。
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

    // ─── 反射工具 ─────────────────────────────────────────────────────────────

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
