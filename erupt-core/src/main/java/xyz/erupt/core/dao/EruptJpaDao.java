package xyz.erupt.core.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.service.EntityManagerService;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.HqlBean;
import xyz.erupt.core.view.Page;

import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2018-10-11.
 */
@Repository
public class EruptJpaDao {

    @Autowired
    private EntityManagerService entityManagerService;

    public void addEntity(Class<?> eruptClass, Object entity) {
        entityManagerService.getEntityManager(eruptClass, (em) -> {
            em.persist(entity);
            em.flush();
        });
    }

    public void editEntity(Class<?> eruptClass, Object entity) {
        entityManagerService.getEntityManager(eruptClass, (em) -> {
            em.merge(entity);
            em.flush();
        });
    }

    public void removeEntity(Class<?> eruptClass, Object entity) {
        entityManagerService.getEntityManager(eruptClass, (em) -> {
            em.remove(entity);
            em.flush();
        });
    }

    public Page queryEruptList(EruptModel eruptModel, Page page, JsonObject searchCondition, String... customCondition) {
        String hql = EruptJpaUtils.generateEruptJpaHql(eruptModel, new HqlBean("new map(" + String.join(",", EruptJpaUtils.getEruptColJapKeys(eruptModel)) + ")",
                customCondition, searchCondition, page.getSort(), false));
        String countHql = EruptJpaUtils.generateEruptJpaHql(eruptModel, new HqlBean("count(*)", customCondition, searchCondition, null, true));
        EntityManager entityManager = entityManagerService.getEntityManager(eruptModel.getClazz());
        Query query = entityManager.createQuery(hql);
        Query countQuery = entityManager.createQuery(countHql);
        Map<String, EruptFieldModel> eruptFieldMap = eruptModel.getEruptFieldMap();
        if (null != searchCondition) {
            for (String key : searchCondition.keySet()) {
                if (!searchCondition.get(key).isJsonNull()) {
                    EruptFieldModel eruptFieldModel = eruptFieldMap.get(key);
                    if (null != eruptFieldModel) {
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
                    }
                    countQuery.setParameter(key, EruptUtil.jsonElementToObject(eruptFieldModel, searchCondition.get(key)));
                    query.setParameter(key, EruptUtil.jsonElementToObject(eruptFieldModel, searchCondition.get(key)));
                }
            }
        }
        page.setTotal((Long) countQuery.getSingleResult());
        if (page.getTotal() > 0) {
            List list = query.setMaxResults(page.getPageSize())
                    .setFirstResult((page.getPageIndex() - 1) * page.getPageSize()).getResultList();
            page.setList(list);
        } else {
            page.setList(new ArrayList<>(0));
        }
        if (entityManager.isOpen()) {
            entityManager.close();
        }
        return page;
    }


    /**
     * @param eruptModel
     * @param cols       格式：name as alias
     * @return
     */
    public List<Map<String, Object>> getDataMap(EruptModel eruptModel, List<String> cols, String sort, String... condition) {
        StringBuilder hql = new StringBuilder();
        hql.append("select new map(").append(String.join(", ", cols)).append(") from ")
                .append(eruptModel.getEruptName()).append(" as ").append(eruptModel.getEruptName());
        ReflectUtil.findClassAllFields(eruptModel.getClazz(), field -> {
            if (null != field.getAnnotation(ManyToOne.class) || null != field.getAnnotation(OneToOne.class)) {
                hql.append(" left outer join ").append(eruptModel.getEruptName()).append(".")
                        .append(field.getName()).append(" as ").append(field.getName());
            }
        });
        hql.append(" where 1 = 1 ");
        for (String s : condition) {
            if (StringUtils.isNotBlank(s)) {
                hql.append(EruptJpaUtils.AND).append(s);
            }
        }
        for (Filter filter : eruptModel.getErupt().filter()) {
            String filterStr = AnnotationUtil.switchFilterConditionToStr(filter);
            if (StringUtils.isNotBlank(filterStr)) {
                hql.append(EruptJpaUtils.AND).append(filterStr);
            }
        }
        hql.append(EruptJpaUtils.geneEruptHqlOrderBy(eruptModel, sort));
        EntityManager entityManager = entityManagerService.getEntityManager(eruptModel.getClazz());
        Query query = entityManager.createQuery(hql.toString());
        List list = query.getResultList();
        if (entityManager.isOpen()) {
            entityManager.close();
        }
        return list;
    }

}
