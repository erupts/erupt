package xyz.erupt.core.service;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.TabType;
import xyz.erupt.core.dao.EruptJapUtils;
import xyz.erupt.core.dao.EruptJpaDao;
import xyz.erupt.core.model.*;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.EruptUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by liyuepeng on 2019-03-06.
 */
@Service
public class DBService implements DataService {

    @Autowired
    private EruptJpaDao eruptJpaDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page queryList(EruptModel eruptModel, JsonObject condition, Page page) {
        return eruptJpaDao.queryEruptList(eruptModel, condition, page);
    }

    @Override
    public Collection findTabListById(EruptModel eruptModel, String tabFieldName, Serializable id) {
        Object obj = eruptJpaDao.findDataById(eruptModel, id);
        try {
            Field tabField = obj.getClass().getDeclaredField(tabFieldName);
            tabField.setAccessible(true);
            Collection collection = (Collection) tabField.get(obj);
            for (Object tabData : collection) {
                EruptUtil.rinseEruptObj(tabData);
            }
            return collection;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List findTabList(EruptFieldModel eruptTabFieldModel) {
        return null;
//        return this.findTabListById(eruptTabFieldModel, null);
    }

    @Override
    public List findTabTreeById(EruptModel eruptModel, String tabFieldName, Serializable id) {
//        EruptModel subEruptModel = InitService.ERUPTS.get(eruptTabFieldModel.getFieldReturnName());
//        String condition = subEruptModel.getEruptName() + "." + subEruptModel.getErupt().primaryKeyCol() + "=" + id;
//        return eruptJpaDao.getDataList(subEruptModel, condition, null,
//                subEruptModel.getEruptName() + "." + subEruptModel.getErupt().primaryKeyCol() + " as " + subEruptModel.getErupt().primaryKeyCol());
        return null;
    }

    @Override
    public List<TreeModel> findTabTree(EruptFieldModel eruptTabFieldModel) {
        EruptModel subEruptModel = InitService.ERUPTS.get(eruptTabFieldModel.getFieldReturnName());
        TabType tabType = eruptTabFieldModel.getEruptField().edit().tabType()[0];
        String condition = null;
        if (!"".equals(tabType.filter().condition())) {
            condition += AnnotationUtil.switchFilterConditionToStr(tabType.filter());
        }
        return treeDataUtil(subEruptModel, condition, null);
    }

    @Override
    public List<TreeModel> queryTree(EruptModel eruptModel) {
        return treeDataUtil(eruptModel, null, null);
    }

    private List<TreeModel> treeDataUtil(EruptModel eruptModel, String condition, String sort) {
        Tree tree = eruptModel.getErupt().tree();
        String[] cols = {
                EruptJapUtils.compleHqlPath(eruptModel.getEruptName(), tree.id()) + " as " + tree.id().replace(".", "_"),
                EruptJapUtils.compleHqlPath(eruptModel.getEruptName(), tree.label()) + " as " + tree.label().replace(".", "_"),
                EruptJapUtils.compleHqlPath(eruptModel.getEruptName(), tree.pid()) + " as " + tree.pid().replace(".", "_")
        };
        List list = eruptJpaDao.getDataMap(eruptModel, condition, sort, cols);
        List<TreeModel> treeModels = new ArrayList<>();
        for (Object o : list) {
            Map<String, Object> map = (Map) o;
            TreeModel treeModel = new TreeModel(map.get(tree.id()), map.get(tree.label()), map.get(tree.pid().replace(".", "_")), null);
            treeModels.add(treeModel);
        }
        return EruptUtil.TreeModelToTree(treeModels);
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Serializable id) {
        return eruptJpaDao.findDataById(eruptModel, id);
    }

    @Override
    public List getReferenceList(EruptModel eruptModel, String fieldName) {
        return eruptJpaDao.getReferenceList(eruptModel, fieldName);
    }

    @Transactional
    @Override
    public void addData(EruptModel eruptModel, Object object) {
        try {
            eruptJpaDao.addEntity(eruptModel, object);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            throw new RuntimeException("数据重复");
        }

    }

    @Transactional
    @Override
    public void editData(EruptModel eruptModel, Object object) {
        eruptJpaDao.editEntity(eruptModel, object);
    }

    @Transactional
    @Override
    public void deleteData(EruptModel eruptModel, Serializable id) {
        Object obj = eruptJpaDao.findDataById(eruptModel, id);
        eruptJpaDao.deleteEntity(obj);
    }
}
