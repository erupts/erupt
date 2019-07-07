package xyz.erupt.core.service.data_impl;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptConst;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.core.bean.EruptFieldModel;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.bean.Page;
import xyz.erupt.core.bean.TreeModel;
import xyz.erupt.core.dao.EruptJpaDao;
import xyz.erupt.core.dao.EruptJpaUtils;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.service.DataService;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.DataHandlerUtil;

import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.*;

/**
 * Created by liyuepeng on 2019-03-06.
 */
@Service
public class DBService implements DataService {

    @Autowired
    private EruptJpaDao eruptJpaDao;

    @Override
    public Object findDataById(EruptModel eruptModel, Serializable id) {
        return eruptJpaDao.findDataById(eruptModel, id);
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, JsonObject searchCondition, String customerCondition) {
        return eruptJpaDao.queryEruptList(eruptModel, page, searchCondition, customerCondition);
    }

    @Override
    public List<TreeModel> queryTree(EruptModel eruptModel) {
        return treeDataUtil(eruptModel, null, null);
    }

    private List<TreeModel> treeDataUtil(EruptModel eruptModel, String condition, String sort) {
        Tree tree = eruptModel.getErupt().tree();
        List<String> cols = new ArrayList<>();
        cols.add(EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), tree.id()) + " as " + EruptConst.ID);
        cols.add(EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), tree.label()) + " as " + EruptConst.LABEL);
        if (StringUtils.isNotBlank(tree.pid())) {
            cols.add(EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), tree.pid()) + " as " + EruptConst.PID);
        }
        List<Map<String, Object>> list = eruptJpaDao.getDataMap(eruptModel, condition, sort, cols, null);
        List<TreeModel> treeModels = new ArrayList<>();
        for (Map map : list) {
            TreeModel treeModel = new TreeModel(map.get(EruptConst.ID), map.get(EruptConst.LABEL), map.get(EruptConst.PID), null);
            treeModels.add(treeModel);
        }
        if (StringUtils.isBlank(tree.pid())) {
            return treeModels;
        } else {
            return DataHandlerUtil.treeModelToTree(treeModels);
        }
    }

    @Transactional
    @Override
    public void addData(EruptModel eruptModel, Object object) {
        try {
            eruptJpaDao.addEntity(eruptModel, object);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException(gcRepeatHint(eruptModel));
        }

    }

    @Transactional
    @Override
    public void editData(EruptModel eruptModel, Object object) {
        try {
            eruptJpaDao.editEntity(eruptModel, object);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            throw new RuntimeException(gcRepeatHint(eruptModel));
        }
    }

    //生成数据重复的提示字符串
    private String gcRepeatHint(EruptModel eruptModel) {
        StringBuilder str = new StringBuilder();
        for (UniqueConstraint uniqueConstraint : eruptModel.getClazz().getAnnotation(Table.class).uniqueConstraints()) {
            for (String columnName : uniqueConstraint.columnNames()) {
                EruptField eruptField = eruptModel.getEruptFieldMap().get(columnName).getEruptField();
                str.append(eruptField.views()[0].title()).append("|");
            }
        }
        return str.substring(0, str.length() - 1) + "重复";
    }

    @Transactional
    @Override
    public void deleteData(EruptModel eruptModel, Serializable id) {
        Object obj = eruptJpaDao.findDataById(eruptModel, id);
        eruptJpaDao.deleteEntity(obj);
    }

    @Override
    public List<TreeModel> findTabTree(EruptModel eruptModel, String fieldName) {
        EruptFieldModel eruptTabFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        EruptModel subEruptModel = CoreService.getErupt(eruptModel.getEruptFieldMap().get(fieldName).getFieldReturnName());
        String condition = AnnotationUtil.switchFilterConditionToStr(eruptTabFieldModel.getEruptField().edit().filter());
        return treeDataUtil(subEruptModel, condition, eruptTabFieldModel.getEruptField().edit().orderBy());
    }


    @Override
    public Collection<TreeModel> getReferenceTree(EruptModel eruptModel, String fieldName, Serializable dependValue) {
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        Edit edit = eruptFieldModel.getEruptField().edit();
        ReferenceTreeType refTree = edit.referenceTreeType();
        List<String> cols = new ArrayList<>();
        cols.add(EruptJpaUtils.completeHqlPath(eruptFieldModel.getFieldReturnName(), refTree.id()) + " as " + refTree.id().replace(".", "_"));
        cols.add(EruptJpaUtils.completeHqlPath(eruptFieldModel.getFieldReturnName(), refTree.label()) + " as " + refTree.label().replace(".", "_"));
        if (StringUtils.isNotBlank(refTree.pid())) {
            cols.add(EruptJpaUtils.completeHqlPath(eruptFieldModel.getFieldName(), refTree.pid()) + " as " + eruptFieldModel.getFieldName() + "_" + refTree.pid());
        }
        StringBuilder condition = new StringBuilder();
        if (!"".equals(edit.filter().condition())) {
            condition.append(AnnotationUtil.switchFilterConditionToStr(edit.filter()));
        }
        //处理depend参数代码
        Map<String, Object> conditionParameter = null;
        if (StringUtils.isNotBlank(refTree.dependField()) && null != dependValue) {
            String DEPEND_KEY = "dependVal";
            conditionParameter = new HashMap<>();
            if (StringUtils.isNotBlank(edit.filter().condition())) {
                condition.append(EruptJpaUtils.AND);
            }
            condition.append(eruptFieldModel.getEruptField().edit().referenceTreeType().dependColumn() + "=:" + DEPEND_KEY);
            conditionParameter.put(DEPEND_KEY, dependValue);
        }
        List<Map<String, Object>> list = eruptJpaDao.getDataMap(CoreService.getErupt(eruptFieldModel.getFieldReturnName()), condition.toString(), null, cols, conditionParameter);
        List<TreeModel> treeModels = new ArrayList<>();
        for (Map<String, Object> map : list) {
            TreeModel treeModel = new TreeModel(map.get(refTree.id()), map.get(refTree.label()), map.get(eruptFieldModel.getFieldName() + "_" + refTree.pid()), null);
            treeModels.add(treeModel);
        }
        if (StringUtils.isBlank(refTree.pid())) {
            return treeModels;
        } else {
            return DataHandlerUtil.treeModelToTree(treeModels);
        }
    }
}
