package xyz.erupt.core.dao;

import xyz.erupt.annotation.sub_field.sub_edit.ReferenceType;
import xyz.erupt.annotation.util.ConfigUtil;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.model.HqlModel;
import xyz.erupt.core.model.Page;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.util.TypeUtil;
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

    public Page queryEruptList(EruptModel eruptModel, JsonObject condition, Page page) {
        StringBuilder conditionStr = new StringBuilder();
        String hql = EruptJapUtils.generateEruptJpaHql(eruptModel, new HqlModel("new map(" + String.join(",", EruptJapUtils.getEruptColJapKeys(eruptModel)) + ")", condition, page.getSort()));
        String countHql = EruptJapUtils.generateEruptJpaHql(eruptModel, new HqlModel("count(*)", condition, null));
        List list = entityManager.createQuery(hql).setMaxResults(page.getPageSize())
                .setFirstResult((page.getPageIndex() - 1) * page.getPageSize()).getResultList();
        page.setTotal((Long) entityManager.createQuery(countHql).getSingleResult());
        page.setList(list);
        return page;
    }


    public List getModelList(EruptModel eruptModel) {
        List list = entityManager.createQuery("from " + eruptModel.getEruptName()).getResultList();
        entityManager.clear();
        return list;
    }


    /**
     * @param eruptModel
     * @param cols       格式：name as alias
     * @return
     */
    public List getDataMap(EruptModel eruptModel, String... cols) {
        String hql = EruptJapUtils.generateEruptJpaHql(eruptModel, new HqlModel("new map(" + String.join(",", cols) + ")", null, null));
        return entityManager.createQuery(hql).getResultList();
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
