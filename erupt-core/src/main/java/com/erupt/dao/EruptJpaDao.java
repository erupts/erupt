package com.erupt.dao;

import com.erupt.annotation.sub_erupt.Filter;
import com.erupt.annotation.sub_field.sub_edit.ReferenceType;
import com.erupt.annotation.util.ConfigUtil;
import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;
import com.erupt.model.Page;
import com.erupt.util.EruptJapUtils;
import com.erupt.util.ReflectUtil;
import com.erupt.util.TypeUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by liyuepeng on 10/11/18.
 */
@Repository
public class EruptJpaDao {

    @PersistenceContext
    private EntityManager entityManager;

    public static final String AND = " and ";

    public static final String CONDITION_KEY = "condition";


    public void saveEntity(EruptModel eruptModel, Object entity) {
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
        Field primaryField = ReflectUtil.findClassField(eruptModel.getClazz(),
                eruptModel.getPrimaryKeyCol());
        id = TypeUtil.typeStrConvertObject(id, primaryField.getType().getSimpleName().toLowerCase());
        return entityManager.find(eruptModel.getClazz(), id);
    }


    public Page queryEruptList(EruptModel eruptModel, JsonObject condition, Page page) {
        String keys = String.join(",", EruptJapUtils.getEruptColJapKeys(eruptModel.getEruptFieldModels()));
        keys = "new map(" + keys + ")";

        Filter filter = eruptModel.getErupt().filter();
        StringBuilder sql = new StringBuilder("select " + keys + " from " + eruptModel.getClazz().getSimpleName());
        if (!"".equals(filter.condition()) || null != condition) {
            sql.append(" where 1=1");
        }
        if (!"".equals(filter.condition())) {
            sql.append(AND).append(ConfigUtil.switchFilterConditionToStr(filter));
        }
        if (null != condition) {
            for (String key : condition.keySet()) {
                sql.append(AND).append(key).append("=").append(condition.get(key));
            }
        }
        List list = entityManager.createQuery(sql.toString())
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
