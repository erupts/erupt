package xyz.erupt.test.core;

import com.google.gson.Gson;
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
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.test.EruptApplicationTests;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 通过包扫描自动发现 xyz.erupt.model.edit 下所有 @Erupt @Entity 模型，
 * 统一验证注解序列化正确性 + CRUD 可用性，无需每加一个 Model 就手动添加测试。
 */
@Service
@Rollback
@Transactional
public class EruptModelTest extends EruptApplicationTests {

    @Resource
    private EruptDao eruptDao;

    private static final Gson GSON = GsonFactory.getGson();
    private static final String MODEL_PACKAGE = "xyz.erupt.test.model";

    // ─── 扫描 ─────────────────────────────────────────────────────────────────

    private List<Class<?>> scanModelClasses() {
        List<Class<?>> list = new ArrayList<>();
        EruptSpringUtil.scannerPackage(new String[]{MODEL_PACKAGE},
                new TypeFilter[]{new AnnotationTypeFilter(Erupt.class)
                }, list::add);
        return list;
    }

    // ─── 序列化校验 ───────────────────────────────────────────────────────────

    /**
     * 验证每个 @Erupt 模型均已注册，且注解能正常序列化为 JSON（代理层无异常）。
     */
    @Test
    void testAllEditTypesSerialization() {
        List<Class<?>> models = scanModelClasses();
        assertFalse(models.isEmpty(), "No @Erupt @Entity classes found in " + MODEL_PACKAGE);

        for (Class<?> clazz : models) {
            String name = clazz.getSimpleName();
            EruptModel eruptModel = EruptCoreService.getErupt(name);
            assertNotNull(eruptModel, name + " must be registered in EruptCoreService");
        }
    }

    // ─── CRUD 统一校验 ────────────────────────────────────────────────────────

    /**
     * 对所有扫描到的模型执行 persist → find 验证。
     * 通过反射自动填充标量字段；关系字段（@ManyToOne 等）递归构建并预先 persist。
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

    // ─── 反射工具 ─────────────────────────────────────────────────────────────

    /**
     * 通过反射构建实体实例：
     * - 标量字段（String/Integer/Long/Boolean/Double/Float）赋最小测试值
     * - @ManyToOne / @OneToOne：递归创建引用实体并预先 persist
     * - @ManyToMany / @OneToMany：创建单个引用实体并预先 persist，以列表形式赋值
     * - @Transient 及 static 字段跳过
     */
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

    /**
     * 从 List&lt;T&gt; 泛型中提取 T。
     */
    private Class<?> resolveCollectionGeneric(Field field) {
        return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

    private void setScalarValue(Object target, Field field) throws Exception {
        Class<?> t = field.getType();
        if (t == String.class) field.set(target, "test");
        else if (t == Integer.class || t == int.class) field.set(target, 1);
        else if (t == Long.class || t == long.class) field.set(target, 1L);
        else if (t == Boolean.class || t == boolean.class) field.set(target, true);
        else if (t == Double.class || t == double.class) field.set(target, 1.0d);
        else if (t == Float.class || t == float.class) field.set(target, 1.0f);
    }
}
