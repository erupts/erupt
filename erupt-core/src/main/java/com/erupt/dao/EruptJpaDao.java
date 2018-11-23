package com.erupt.dao;

import com.erupt.annotation.sub_erupt.Filter;
import com.erupt.annotation.sub_field.sub_edit.ReferenceType;
import com.erupt.annotation.util.ConfigUtil;
import com.erupt.core.model.EruptFieldModel;
import com.erupt.core.model.EruptModel;
import com.erupt.core.model.Page;
import com.erupt.util.ReflectUtil;
import com.erupt.util.TypeUtil;
import com.google.gson.JsonObject;
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

    public static final String NULL = "$null$";

    public static final String NOT_NULL = "$notNull$";


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
        Field primaryField = ReflectUtil.findClassAllField(eruptModel.getClazz(),
                eruptModel.getPrimaryKeyCol());
        id = TypeUtil.typeStrConvertObject(id, primaryField.getType().getSimpleName().toLowerCase());
        return entityManager.find(eruptModel.getClazz(), id);
    }


    public Page queryEruptListByValidate(EruptModel eruptModel, JsonObject condition, Page page) {
        boolean isPass = false;
        for (String key : condition.keySet()) {
            String _key = key.split("\\.")[0];
            isPass = null != eruptModel.getEruptFieldMap().get(_key).toString() &&
                    eruptModel.getEruptFieldMap().get(_key).getEruptField().edit().search().isSearch();
        }
        if (isPass || condition.keySet().size() == 0) {
            return queryEruptList(eruptModel, condition, page);
        } else {
            return new Page();
        }
    }

    public Page queryEruptList(EruptModel eruptModel, JsonObject condition, Page page) {
        Filter filter = eruptModel.getErupt().filter();
        StringBuilder sql = new StringBuilder(EruptJapUtils.generateEruptJpaHql(eruptModel));
        sql.append("where 1=1");
        if (!"".equals(filter.condition())) {
            sql.append(AND).append(ConfigUtil.switchFilterConditionToStr(filter));
        }
        if (null != condition) {
            for (String key : condition.keySet()) {
                String _key = key;
                if (!_key.contains(".")) {
                    _key = eruptModel.getEruptName() + "." + key;
                }
                if (condition.get(key).toString().contains(NULL)) {
                    sql.append(AND).append(_key).append(" is null");
                } else if (condition.get(key).toString().contains(NOT_NULL)) {
                    sql.append(AND).append(_key).append(" is not null");
                } else {
                    sql.append(AND).append(_key).append("=").append(condition.get(key));
                }
            }
        }
        List list = entityManager.createQuery(sql.toString())
                .setMaxResults(page.getPageSize())
                .setFirstResult((page.getPageNumber() - 1) * page.getPageSize())
                .getResultList();
        page.setList(list);
        return page;
    }


    public List getModelList(EruptModel eruptModel) {
        List list = entityManager.createQuery("from " + eruptModel.getEruptName()).getResultList();
        entityManager.clear();
        return list;
    }

    /**
     * @param col 参数组成形式 {列名} as {别名}
     */
    public List getDataMap(EruptModel eruptModel, String... col) {
        String keys = "new map(" + String.join(",", col) + ")";
        String hql = "select " + keys + " from " + eruptModel.getEruptName() + " where 1=1 ";
        Filter filter = eruptModel.getErupt().filter();
        if (!"".equals(filter.condition())) {
            hql += AND + ConfigUtil.switchFilterConditionToStr(filter);
        }
        return entityManager.createQuery(hql)
                .getResultList();
    }

    public List getReferenceList(EruptModel eruptModel, String refName) {
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(refName);
        ReferenceType referenceType = eruptFieldModel.getEruptField().edit().referenceType()[0];
        String keys = "new map(" + referenceType.id() + " as id," + referenceType.label() + " as label)";
        String hql = "select " + keys + " from " + eruptFieldModel.getField().getType().getSimpleName() + " where 1=1 ";
        if (!"".equals(referenceType.filter().condition())) {
            hql += AND + ConfigUtil.switchFilterConditionToStr(referenceType.filter());
        }
        if (!"".equals(referenceType.pid())) {
            hql += AND + "";
        }
        return entityManager.createQuery(hql)
                .getResultList();
    }

    public static void main(String[] args) {
        System.out.println(int.class);
    }
}
