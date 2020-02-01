package xyz.erupt.core.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Repository;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.bean.EruptFieldModel;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.bean.HqlBean;
import xyz.erupt.core.bean.Page;
import xyz.erupt.core.util.EruptUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2018-10-11.
 */
@Repository
public class EruptJpaDao {

    @PersistenceContext
    private EntityManager entityManager;

    public void addEntity(Object entity) {
        entityManager.persist(entity);
    }

    public void editEntity(Object entity) {
        entityManager.merge(entity);
    }

    public Object findDataById(EruptModel eruptModel, Object id) {
        return entityManager.find(eruptModel.getClazz(), id);
    }

    public Page queryEruptList(EruptModel eruptModel, Page page, JsonObject searchCondition, String customCondition) {
        String hql = EruptJpaUtils.generateEruptJpaHql(eruptModel, new HqlBean("new map(" + String.join(",", EruptJpaUtils.getEruptColJapKeys(eruptModel)) + ")",
                customCondition, searchCondition, page.getSort(), false));
        String countHql = EruptJpaUtils.generateEruptJpaHql(eruptModel, new HqlBean("count(*)", customCondition, searchCondition, null, true));
        Query query = entityManager.createQuery(hql);
        Query countQuery = entityManager.createQuery(countHql);

        Map<String, EruptFieldModel> eruptFieldMap = eruptModel.getEruptFieldMap();
        if (null != searchCondition) {
            for (String key : searchCondition.keySet()) {
                if (!searchCondition.get(key).isJsonNull()) {
                    EruptFieldModel eruptFieldModel = eruptFieldMap.get(key);
                    Edit edit = eruptFieldModel.getEruptField().edit();
                    if (edit.search().vague()) {
                        if ((edit.type() == EditType.NUMBER) || edit.type() == EditType.DATE || edit.type() == EditType.SLIDER) {
                            JsonArray jsonArray = searchCondition.get(key).getAsJsonArray();
                            countQuery.setParameter(EruptJpaUtils.LVAL_KEY + key, EruptUtil.jsonElementToObject(eruptFieldModel, jsonArray.get(0)));
                            countQuery.setParameter(EruptJpaUtils.RVAL_KEY + key, EruptUtil.jsonElementToObject(eruptFieldModel, jsonArray.get(1)));
                            query.setParameter(EruptJpaUtils.LVAL_KEY + key, EruptUtil.jsonElementToObject(eruptFieldModel, jsonArray.get(0)));
                            query.setParameter(EruptJpaUtils.RVAL_KEY + key, EruptUtil.jsonElementToObject(eruptFieldModel, jsonArray.get(1)));
                            continue;
                        } else if (edit.type() == EditType.INPUT) {
                            countQuery.setParameter(key, EruptJpaUtils.PERCENT + EruptUtil.jsonElementToObject(eruptFieldModel, searchCondition.get(key)) + EruptJpaUtils.PERCENT);
                            query.setParameter(key, EruptJpaUtils.PERCENT + EruptUtil.jsonElementToObject(eruptFieldModel, searchCondition.get(key)) + EruptJpaUtils.PERCENT);
                            continue;
                        }
                    }
                    countQuery.setParameter(key, EruptUtil.jsonElementToObject(eruptFieldModel, searchCondition.get(key)));
                    query.setParameter(key, EruptUtil.jsonElementToObject(eruptFieldModel, searchCondition.get(key)));
                }
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
    public List<Map<String, Object>> getDataMap(EruptModel eruptModel, String condition,
                                                String orderBy, List<String> cols,
                                                Map<String, Object> conditionParameter) {
        String hql = EruptJpaUtils.generateEruptJpaHql(eruptModel, new HqlBean("new map(" + String.join(",", cols) + ")", condition, null, orderBy, false));
        Query query = entityManager.createQuery(hql);
        if (null != conditionParameter) {
            for (Map.Entry<String, Object> entry : conditionParameter.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
        }
        return query.getResultList();
    }


//    public List getDataList(EruptModel eruptModel, String condition, String orderBy, String... cols) {
//        String hql = EruptJapUtils.generateEruptJpaHql(eruptModel, new HqlModel(String.join(",", cols), condition, null, orderBy));
//        return entityManager.createQuery(hql).getResultList();
//    }

}
