package xyz.erupt.jpa.dao;

import jakarta.annotation.Resource;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.query.Aggregate;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.jpa.service.EntityManagerService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2018-10-11.
 */
@Repository
public class EruptJpaDao {

    @Resource
    private EntityManagerService entityManagerService;

    @Transactional
    public void addEntity(Class<?> eruptClass, Object entity) {
        entityManagerService.entityManagerTran(eruptClass, (em) -> em.persist(entity));
    }

    @Transactional
    public void editEntity(Class<?> eruptClass, Object entity) {
        entityManagerService.entityManagerTran(eruptClass, (em) -> em.merge(entity));
    }

    @Transactional
    public void removeEntity(Class<?> eruptClass, Object entity) {
        entityManagerService.entityManagerTran(eruptClass, (em) -> {
            if (em.contains(entity)) {
                em.remove(entity);
            } else {
                em.remove(em.merge(entity));
            }
        });
    }

    public Page queryEruptList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        String hql = EruptJpaUtils.generateEruptJpaHql(eruptModel, "new map(" + String.join(",", EruptJpaUtils.getEruptColJpaKeys(eruptModel)) + ")", eruptQuery, false);
        String countHql = EruptJpaUtils.generateEruptJpaHql(eruptModel, "count(*)", eruptQuery, true);
        return entityManagerService.getEntityManager(eruptModel.getClazz(), entityManager -> {
            @SuppressWarnings("SqlSourceToSinkFlow")
            Query query = entityManager.createQuery(hql);
            @SuppressWarnings("SqlSourceToSinkFlow")
            Query countQuery = entityManager.createQuery(countHql);
            bindConditions(eruptModel, eruptQuery, query, countQuery);
            page.setTotal((Long) countQuery.getSingleResult());
            if (page.getTotal() > 0) {
                page.setList(query.setMaxResults(page.getPageSize()).setFirstResult((page.getPageIndex() - 1) * page.getPageSize()).getResultList());
            } else {
                page.setList(new ArrayList<>(0));
            }
            return page;
        });
    }

    /**
     * Column totals over every row the query matches, in one SQL round trip:
     * {@code select new map(sum(E.amount) as a0, count(distinct E.owner) as a1, ...)} with the
     * same joins and where clause as the list query.
     */
    public Map<String, Object> queryEruptAggregate(EruptModel eruptModel, EruptQuery eruptQuery, List<Aggregate> aggregates) {
        List<String> cols = new ArrayList<>();
        for (int i = 0; i < aggregates.size(); i++) {
            Aggregate aggregate = aggregates.get(i);
            String path = EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), EruptJpaUtils.legalPath(aggregate.getPath()));
            cols.add(aggregateHql(aggregate.getStatistic(), path) + EruptJpaUtils.AS + "a" + i);
        }
        String hql = EruptJpaUtils.generateEruptJpaHql(eruptModel, "new map(" + String.join(", ", cols) + ")", eruptQuery, true);
        return entityManagerService.getEntityManager(eruptModel.getClazz(), entityManager -> {
            @SuppressWarnings("SqlSourceToSinkFlow")
            Query query = entityManager.createQuery(hql);
            bindConditions(eruptModel, eruptQuery, query);
            @SuppressWarnings("unchecked")
            Map<String, Object> row = (Map<String, Object>) query.getSingleResult();
            Map<String, Object> result = new LinkedHashMap<>();
            for (int i = 0; i < aggregates.size(); i++) {
                result.put(aggregates.get(i).getKey(), row.get("a" + i));
            }
            return result;
        });
    }

    private static String aggregateHql(View.Statistic statistic, String path) {
        switch (statistic) {
            case COUNT:
                return "count(" + path + ")";
            case DISTINCT_COUNT:
                return "count(distinct " + path + ")";
            case SUM:
                return "sum(" + path + ")";
            case AVG:
                return "avg(" + path + ")";
            case MAX:
                return "max(" + path + ")";
            case MIN:
                return "min(" + path + ")";
            default:
                throw new IllegalArgumentException("no aggregate for " + statistic);
        }
    }

    /**
     * Binds the typed search conditions of a query onto every JPA query built from the same
     * hql; the parameter names are the condition keys with dots replaced, as the hql
     * generator writes them.
     */
    private static void bindConditions(EruptModel eruptModel, EruptQuery eruptQuery, Query... queries) {
        if (null == eruptQuery.getConditions()) return;
        Map<String, EruptFieldModel> eruptFieldMap = eruptModel.getEruptFieldMap();
        for (Condition condition : eruptQuery.getConditions()) {
            EruptFieldModel eruptFieldModel = eruptFieldMap.get(condition.getKey());
            condition.setKey(condition.getKey().replace(EruptConst.DOT, "_"));
            for (Query query : queries) {
                switch (condition.getExpression()) {
                    case EQ:
                    case NEQ:
                    case GT:
                    case GTE:
                    case LT:
                    case LTE:
                        query.setParameter(condition.getKey(), EruptUtil.convertObjectType(eruptFieldModel, condition.getValue()));
                        break;
                    case LIKE:
                    case NOT_LIKE:
                        query.setParameter(condition.getKey(), EruptJpaUtils.PERCENT + condition.getValue() + EruptJpaUtils.PERCENT);
                        break;
                    case RANGE:
                        List<?> list = (List<?>) condition.getValue();
                        query.setParameter(EruptJpaUtils.L_VAL_KEY + condition.getKey(), EruptUtil.convertObjectType(eruptFieldModel, list.get(0)));
                        query.setParameter(EruptJpaUtils.R_VAL_KEY + condition.getKey(), EruptUtil.convertObjectType(eruptFieldModel, list.get(1)));
                        break;
                    case IN:
                    case NOT_IN:
                        List<Object> listIn = new ArrayList<>();
                        for (Object o : (List<?>) condition.getValue()) {
                            listIn.add(EruptUtil.convertObjectType(eruptFieldModel, o));
                        }
                        query.setParameter(condition.getKey(), listIn);
                        break;
                    case NULL:
                    case NOT_NULL:
                        break;
                }
            }
        }
    }

}
