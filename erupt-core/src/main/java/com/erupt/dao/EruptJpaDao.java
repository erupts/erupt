package com.erupt.dao;

import com.erupt.annotation.sub_field.View;
import com.erupt.annotation.sub_field.sub_edit.ReferenceType;
import com.erupt.annotation.util.ConfigUtil;
import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;
import com.erupt.model.Page;
import com.erupt.service.CoreService;
import com.erupt.util.EruptUtil;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuepeng on 10/11/18.
 */
@Repository
public class EruptJpaDao {

    @PersistenceContext
    private EntityManager entityManager;


    public static final String spaceSymRegex = "\\{\\{[a-z0-9A-Z_$]+\\}\\}";


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
        //将参数id转换为主键标识类型
//        for (EruptFieldModel field : eruptModel.getEruptFieldModels()) {
//            if (field.getField().getAnnotation(Id.class) != null) {
//                id = TypeUtil.typeStrConvertObject(id, field.getField().getType().getSimpleName().toLowerCase());
//                break;
//            }
//        }
        return entityManager.find(eruptModel.getClazz(), id);
    }


    public Page queryEruptList(EruptModel eruptModel, Page page) {
        String keys = String.join(",", EruptJapUtils.getEruptColJapKeys(eruptModel.getEruptFieldModels()));
        keys = "new map(" + keys + ")";
        List list = entityManager.createQuery("select " + keys + " from " + eruptModel.getClazz().getSimpleName())
                .setMaxResults(page.getPageSize())
                .setFirstResult((page.getPageNumber() - 1) * page.getPageSize())
                .getResultList();
        page.setList(list);
        return page;
    }

    /**
     * @param modelName
     * @param col       参数组成形式 {列名} as {别名}
     */
    public List getDataMap(String modelName, String... col) {
        String keys = "new map(" + String.join(",", col) + ")";
        return entityManager.createQuery("select " + keys + " from " + modelName)
                .getResultList();
    }

    public List getReferenceList(EruptModel eruptModel, String refName) {
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(refName);
        ReferenceType referenceType = eruptFieldModel.getEruptField().edit().referenceType()[0];
        String keys = "new map(" + referenceType.id() + " as id," + referenceType.label() + " as label)";
        String hql = "select " + keys + " from " + eruptFieldModel.getField().getType().getSimpleName();
        if (!"".equals(referenceType.filter().condition())) {
            hql += " where " + ConfigUtil.switchFilterConditionToStr(referenceType.filter());
        }
        return entityManager.createQuery(hql)
                .getResultList();
    }

}
