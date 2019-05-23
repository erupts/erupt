package xyz.erupt.core.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.model.HqlModel;
import xyz.erupt.core.model.Page;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.util.TypeUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by liyuepeng on 10/11/18.
 */
@Repository
public class EruptJpaDao {

    @PersistenceContext
    private EntityManager entityManager;

    public void addEntity(EruptModel eruptModel, Object entity) {
        entityManager.persist(entity);
    }

    public void editEntity(EruptModel eruptModel, Object entity) {
        entityManager.merge(entity);
    }

    public void deleteEntity(Object entity) {
        if (null != entity) {
            entityManager.remove(entity);
        }
    }

    public Object findDataById(EruptModel eruptModel, Serializable id) {
        Field primaryField = ReflectUtil.findClassField(eruptModel.getClazz(), eruptModel.getErupt().primaryKeyCol());
        id = TypeUtil.typeStrConvertObject(id, primaryField.getType().getSimpleName().toLowerCase());
        Object obj = entityManager.find(eruptModel.getClazz(), id);
        EruptUtil.rinseEruptObj(obj);
        return obj;
    }

    public Page queryEruptList(EruptModel eruptModel, JsonObject condition, Page page) {
        String hql = EruptJapUtils.generateEruptJpaHql(eruptModel, new HqlModel("new map(" + String.join(",", EruptJapUtils.getEruptColJapKeys(eruptModel)) + ")", condition, page.getSort()));
        String countHql = EruptJapUtils.generateEruptJpaHql(eruptModel, new HqlModel("count(*)", condition, null));
        Query query = entityManager.createQuery(hql);
        Query countQuery = entityManager.createQuery(countHql);
        Map<String, EruptFieldModel> eruptFieldMap = eruptModel.getEruptFieldMap();
        for (String key : condition.keySet()) {
            if (!condition.get(key).isJsonNull()) {
                EruptFieldModel eruptFieldModel = eruptFieldMap.get(key);
                Edit edit = eruptFieldModel.getEruptField().edit();
                if (edit.search().vague()) {
                    if ((edit.type() == EditType.INPUT && eruptFieldModel.getFieldReturnName().equals(EruptFieldModel.NUMBER_TYPE))
                            || edit.type() == EditType.DATE || edit.type() == EditType.SLIDER) {
                        JsonArray jsonArray = condition.get(key).getAsJsonArray();
                        countQuery.setParameter(EruptJapUtils.LVAL_KEY + key, EruptUtil.jsonElementToObject(eruptFieldModel, jsonArray.get(0)));
                        countQuery.setParameter(EruptJapUtils.RVAL_KEY + key, EruptUtil.jsonElementToObject(eruptFieldModel, jsonArray.get(1)));
                        query.setParameter(EruptJapUtils.LVAL_KEY + key, EruptUtil.jsonElementToObject(eruptFieldModel, jsonArray.get(0)));
                        query.setParameter(EruptJapUtils.RVAL_KEY + key, EruptUtil.jsonElementToObject(eruptFieldModel, jsonArray.get(1)));
                        continue;
                    }
                }
                countQuery.setParameter(key, EruptUtil.jsonElementToObject(eruptFieldModel, condition.get(key)));
                query.setParameter(key, EruptUtil.jsonElementToObject(eruptFieldModel, condition.get(key)));
            }
        }
        List list = query.setMaxResults(page.getPageSize())
                .setFirstResult((page.getPageIndex() - 1) * page.getPageSize()).getResultList();
        page.setTotal((Long) countQuery.getSingleResult());
        page.setList(list);
        return page;
    }


    /**
     * @param eruptModel
     * @param cols       格式：name as alias
     * @return
     */
    public List getDataMap(EruptModel eruptModel, String condition, String orderBy, List<String> cols, Map<String, Object> conditionParameter) {
        String hql = EruptJapUtils.generateEruptJpaHql(eruptModel, new HqlModel("new map(" + String.join(",", cols) + ")", condition, null, orderBy));
        Query query = entityManager.createQuery(hql);
        if (null != conditionParameter) {
            for (String key : conditionParameter.keySet()) {
                query.setParameter(key, conditionParameter.get(key));
            }
        }
        return query.getResultList();
    }


    public List getDataList(EruptModel eruptModel, String condition, String orderBy, String... cols) {
        String hql = EruptJapUtils.generateEruptJpaHql(eruptModel, new HqlModel(String.join(",", cols), condition, null, orderBy));
        return entityManager.createQuery(hql).getResultList();
    }


    public List getReferenceList(EruptModel eruptModel, String refName) {
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(refName);
        ReferenceTreeType referenceTreeType = eruptFieldModel.getEruptField().edit().referenceTreeType()[0];
        String keys = "new map(" + referenceTreeType.id() + " as id," + referenceTreeType.label() + " as label)";
        String hql = EruptJapUtils.generateEruptJpaHql(eruptModel, new HqlModel(keys, null, null, null));
        if (!"".equals(referenceTreeType.filter().condition())) {
            hql += EruptJapUtils.AND + AnnotationUtil.switchFilterConditionToStr(referenceTreeType.filter());
        }
        return entityManager.createQuery(hql).getResultList();
    }

}
