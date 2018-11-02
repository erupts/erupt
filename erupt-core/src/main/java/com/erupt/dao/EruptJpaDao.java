package com.erupt.dao;

import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;
import com.erupt.model.Page;
import com.erupt.util.EruptUtil;
import com.erupt.util.TypeUtil;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

/**
 * Created by liyuepeng on 10/11/18.
 */
@Repository
public class EruptJpaDao {

    @PersistenceContext
    private EntityManager entityManager;

    static {

    }

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
        List<String> fieldNames = EruptUtil.getEruptFieldNames(eruptModel.getEruptFieldModels());
        List list = entityManager.createQuery("from " + eruptModel.getClazz().getSimpleName())
                .setMaxResults(page.getPageSize())
                .setFirstResult((page.getPageNumber() - 1) * page.getPageSize())
                .getResultList();
        //将未使用erupt修饰的字段值置空
        for (Object o : list) {
            EruptUtil.converEruptFieldInfo(o);
        }
        page.setList(list);

        return page;
    }

    /**
     *
     * @param modelName
     * @param col 参数组成形式 {列名} as {别名}
     */
    public List getDataMap(String modelName, String... col) {
        return entityManager.createQuery("select new map(" + String.join(",", col) + ") from " + modelName).getResultList();
    }

}
