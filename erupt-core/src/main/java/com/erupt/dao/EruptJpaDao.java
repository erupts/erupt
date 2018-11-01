package com.erupt.dao;

import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;
import com.erupt.model.Page;
import com.erupt.util.EruptUtil;
import com.erupt.util.TypeUtil;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

/**
 * Created by liyuepeng on 10/11/18.
 */
@Repository
public class EruptJpaDao {

    @PersistenceContext
    private EntityManager entityManager;

    public void saveEntity(Object entity) {
        entityManager.persist(entity);
    }

    private void motifyData(Object ob) {

    }

    public void deleteEntity(Object entity) {
        if (null != entity) {
            entityManager.remove(entity);
        }
    }

    public Object findDataById(EruptModel eruptModel, Serializable id) {
        Object objectId = null;
        for (EruptFieldModel field : eruptModel.getEruptFieldModels()) {
            if (field.getField().getAnnotation(Id.class) != null) {
                objectId = TypeUtil.typeStrConvertObject(id, field.getField().getType().getSimpleName().toLowerCase());
                break;
            }
        }
        return entityManager.find(eruptModel.getClazz(), objectId);
    }

    public Object queryByPage(Class<?> entity) {
        return entityManager.createQuery("from " + entity.getSimpleName());
    }

    ;

    public Page queryByPage(EruptModel eruptModel, Page page) {
        List<String> fieldNames = EruptUtil.getEruptFieldNames(eruptModel.getEruptFieldModels());
        page.setList(entityManager.createQuery("from " + eruptModel.getClazz().getSimpleName())
                .setMaxResults(page.getPageSize())
                .setFirstResult((page.getPageNumber() - 1) * page.getPageSize())
                .getResultList());
        return page;
    }


}
