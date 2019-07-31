package xyz.erupt.core.service.data_impl;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
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
import xyz.erupt.core.util.ReflectUtil;

import javax.persistence.*;
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
        cols.add(EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), tree.id()) + " as " + AnnotationConst.ID);
        cols.add(EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), tree.label()) + " as " + AnnotationConst.LABEL);
        if (StringUtils.isNotBlank(tree.pid())) {
            cols.add(EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), tree.pid()) + " as " + AnnotationConst.PID);
        }
        List<Map<String, Object>> list = eruptJpaDao.getDataMap(eruptModel, condition, sort, cols, null);
        List<TreeModel> treeModels = new ArrayList<>();
        for (Map map : list) {
            TreeModel treeModel = new TreeModel(map.get(AnnotationConst.ID), map.get(AnnotationConst.LABEL), map.get(AnnotationConst.PID), null);
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
            jpaManyToOneConvert(eruptModel, object);
            eruptJpaDao.addEntity(object);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException(gcRepeatHint(eruptModel));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @Override
    public void editData(EruptModel eruptModel, Object data) {
        try {
            //没有用eruptField修饰但是使用了hibernate管理的字段 设置为数据库原有存储值
            Object entity = entityManager.find(eruptModel.getClazz(),
                    ReflectUtil.findClassField(data.getClass(), eruptModel.getErupt().primaryKeyCol()).get(data));
            ReflectUtil.findClassAllFields(entity.getClass(), field -> {
                try {
                    if (null == field.getAnnotation(EruptField.class) && null == field.getAnnotation(Transient.class)) {
                        field.setAccessible(true);
                        Field dataField = data.getClass().getDeclaredField(field.getName());
                        dataField.setAccessible(true);
                        dataField.set(data, field.get(entity));
                    }
                    //删除原始一对多数据
                    if (null != field.getAnnotation(OneToMany.class) && null != field.getAnnotation(EruptField.class)) {
                        field.setAccessible(true);
                        Collection collection = (Collection) field.get(entity);
                        for (Object o : collection) {
                            entityManager.remove(o);
                        }
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            });
            jpaManyToOneConvert(eruptModel, data);
            eruptJpaDao.editEntity(data);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            throw new RuntimeException(gcRepeatHint(eruptModel));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    //@ManyToOne数据处理
    private void jpaManyToOneConvert(EruptModel eruptModel, Object object) throws NoSuchFieldException, IllegalAccessException {
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            if (fieldModel.getEruptField().edit().type() == EditType.TAB_TABLE_ADD) {
                Field field = object.getClass().getDeclaredField(fieldModel.getFieldName());
                field.setAccessible(true);
                Collection collection = (Collection) field.get(object);
                OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                for (Object o : collection) {
                    Field ff = o.getClass().getDeclaredField(oneToMany.mappedBy());
                    ff.setAccessible(true);
                    ff.set(o, object);
                    //删除主键ID
                    Field pk = o.getClass().getDeclaredField(CoreService
                            .getErupt(fieldModel.getFieldReturnName()).getErupt().primaryKeyCol());
                    pk.setAccessible(true);
                    pk.set(o, null);
                }
            }
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
        return str.substring(0, str.length() - 1) + "数据重复";
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
