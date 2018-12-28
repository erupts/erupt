package xyz.erupt.dao;

import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceType;
import xyz.erupt.annotation.util.ConfigUtil;
import xyz.erupt.base.model.EruptFieldModel;
import xyz.erupt.base.model.EruptModel;
import xyz.erupt.base.model.HqlModel;
import xyz.erupt.base.model.Page;
import xyz.erupt.util.ReflectUtil;
import xyz.erupt.util.TypeUtil;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import static xyz.erupt.dao.EruptJapUtils.getEruptColJapKeys;

/**
 * Created by liyuepeng on 10/11/18.
 */
@Repository
public class EruptJpaDao {

    @PersistenceContext
    private EntityManager entityManager;

    public static final String COUNT_COL_NAME = "count";


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
                eruptModel.getErupt().primaryKeyCol());
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
        StringBuilder hql = new StringBuilder(EruptJapUtils.generateEruptJpaHql(eruptModel,
                new HqlModel("new map(" + String.join(",", getEruptColJapKeys(eruptModel)) + ")")));
        StringBuilder conditionStr = new StringBuilder();
        if (null != condition) {
            for (String key : condition.keySet()) {
                String _key = key;
                if (!_key.contains(".")) {
                    _key = eruptModel.getEruptName() + "." + key;
                }
                if (condition.get(key).toString().contains(EruptJapUtils.NULL)) {
                    conditionStr.append(EruptJapUtils.AND).append(_key).append(" is null");
                } else if (condition.get(key).toString().contains(EruptJapUtils.NOT_NULL)) {
                    conditionStr.append(EruptJapUtils.AND).append(_key).append(" is not null");
                } else {
                    conditionStr.append(EruptJapUtils.AND).append(_key).append("=").append(condition.get(key));
                }
            }
        }
        hql.append(conditionStr);
        StringBuilder countHql = new StringBuilder(EruptJapUtils.generateEruptJpaHql(eruptModel,
                new HqlModel("count(*)")));
        countHql.append(conditionStr);
        Long total = (Long) entityManager.createQuery(countHql.toString()).getSingleResult();
        //page Object
        List list = entityManager.createQuery(hql.toString())
                .setMaxResults(page.getPageSize())
                .setFirstResult((page.getPageIndex() - 1) * page.getPageSize())
                .getResultList();
        page.setTotal(total);
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
        String hql = EruptJapUtils.generateEruptJpaHql(eruptModel, new HqlModel("new map(" + String.join(",", col) + ")"));
        return entityManager.createQuery(hql)
                .getResultList();
    }

    public List getReferenceList(EruptModel eruptModel, String refName) {
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(refName);
        ReferenceType referenceType = eruptFieldModel.getEruptField().edit().referenceType()[0];
        String keys = "new map(" + referenceType.id() + " as id," + referenceType.label() + " as label)";
        String hql = "select " + keys + " from " + eruptFieldModel.getField().getType().getSimpleName() + " where 1=1 ";
        if (!"".equals(referenceType.filter().condition())) {
            hql += EruptJapUtils.AND + ConfigUtil.switchFilterConditionToStr(referenceType.filter());
        }
        if (!"".equals(referenceType.pid())) {
//            hql += AND + referenceType.pid();
        }
        return entityManager.createQuery(hql)
                .getResultList();
    }

}
