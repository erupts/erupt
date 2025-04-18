package xyz.erupt.jpa.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.jpa.service.EntityManagerService;

import jakarta.annotation.Resource;
import jakarta.persistence.Query;
import java.util.ArrayList;
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
            Query query = entityManager.createQuery(hql);
            Query countQuery = entityManager.createQuery(countHql);
            Map<String, EruptFieldModel> eruptFieldMap = eruptModel.getEruptFieldMap();
            if (null != eruptQuery.getConditions()) {
                for (Condition condition : eruptQuery.getConditions()) {
                    EruptFieldModel eruptFieldModel = eruptFieldMap.get(condition.getKey());
                    condition.setKey(condition.getKey().replace(EruptConst.DOT, "_"));
                    switch (condition.getExpression()) {
                        case EQ:
                            countQuery.setParameter(condition.getKey(), EruptUtil.convertObjectType(eruptFieldModel, condition.getValue()));
                            query.setParameter(condition.getKey(), EruptUtil.convertObjectType(eruptFieldModel, condition.getValue()));
                            break;
                        case LIKE:
                            countQuery.setParameter(condition.getKey(), EruptJpaUtils.PERCENT + condition.getValue() + EruptJpaUtils.PERCENT);
                            query.setParameter(condition.getKey(), EruptJpaUtils.PERCENT + condition.getValue() + EruptJpaUtils.PERCENT);
                            break;
                        case RANGE:
                            List<?> list = (List<?>) condition.getValue();
                            countQuery.setParameter(EruptJpaUtils.L_VAL_KEY + condition.getKey(), EruptUtil.convertObjectType(eruptFieldModel, list.get(0)));
                            countQuery.setParameter(EruptJpaUtils.R_VAL_KEY + condition.getKey(), EruptUtil.convertObjectType(eruptFieldModel, list.get(1)));
                            query.setParameter(EruptJpaUtils.L_VAL_KEY + condition.getKey(), EruptUtil.convertObjectType(eruptFieldModel, list.get(0)));
                            query.setParameter(EruptJpaUtils.R_VAL_KEY + condition.getKey(), EruptUtil.convertObjectType(eruptFieldModel, list.get(1)));
                            break;
                        case IN:
                            List<Object> listIn = new ArrayList<>();
                            for (Object o : (List<?>) condition.getValue()) {
                                listIn.add(EruptUtil.convertObjectType(eruptFieldModel, o));
                            }
                            countQuery.setParameter(condition.getKey(), listIn);
                            query.setParameter(condition.getKey(), listIn);
                            break;
                    }
                }
            }
            page.setTotal((Long) countQuery.getSingleResult());
            if (page.getTotal() > 0) {
                page.setList(query.setMaxResults(page.getPageSize()).setFirstResult((page.getPageIndex() - 1) * page.getPageSize()).getResultList());
            } else {
                page.setList(new ArrayList<>(0));
            }
            return page;
        });
    }

}
