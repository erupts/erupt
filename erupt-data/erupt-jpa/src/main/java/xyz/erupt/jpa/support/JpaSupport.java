package xyz.erupt.jpa.support;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.JoinColumn;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.annotation.EruptDataSource;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.jpa.dao.EruptDao;

import java.lang.reflect.Field;

@Component
public class JpaSupport {

    @Resource
    private EruptDao eruptDao;

    /**
     * adaptation jpa → @JoinColumn → referencedColumnName
     */
    @SneakyThrows
    public void referencedColumnNameSupport(Object obj, Field field) {
        EruptField eruptField = field.getAnnotation(EruptField.class);
        if (null != eruptField) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (null != joinColumn && !"".equals(joinColumn.referencedColumnName())) {
                String id;
                if (eruptField.edit().type() == EditType.REFERENCE_TREE) {
                    id = eruptField.edit().referenceTreeType().id();
                } else if (eruptField.edit().type() == EditType.REFERENCE_TABLE) {
                    id = eruptField.edit().referenceTableType().id();
                } else {
                    return;
                }
                field.setAccessible(true);
                Object refObject = field.get(obj);
                if (null != refObject) {
                    Field idField = ReflectUtil.findClassField(refObject.getClass(), id);
                    idField.setAccessible(true);
                    EntityManager em = eruptDao.getEntityManager();
                    EruptDataSource eruptDataSource = refObject.getClass().getAnnotation(EruptDataSource.class);
                    if (eruptDataSource != null) {
                        em = eruptDao.getEntityManager(eruptDataSource.value());
                    }
                    Object result = em.createQuery("from " + refObject.getClass().getSimpleName() + " I where I.id = :id")
                            .setParameter("id", idField.get(refObject)).getSingleResult();
                    em.close();
                    field.set(obj, result);
                }
            }
        }

    }

}