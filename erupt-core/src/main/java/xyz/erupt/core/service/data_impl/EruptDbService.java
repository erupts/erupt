package xyz.erupt.core.service.data_impl;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.core.dao.EruptJpaDao;
import xyz.erupt.core.dao.EruptJpaUtils;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.query.Condition;
import xyz.erupt.core.query.Order;
import xyz.erupt.core.query.Query;
import xyz.erupt.core.service.EntityManagerService;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptDataService;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.DataHandlerUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TreeModel;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author liyuepeng
 * @date 2019-03-06.
 */
@Service
public class EruptDbService implements EruptDataService {

    @Autowired
    private EruptJpaDao eruptJpaDao;

    @Autowired
    private EntityManagerService entityManagerService;

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        EntityManager entityManager = entityManagerService.getEntityManager(eruptModel);
        Object obj = entityManager.find(eruptModel.getClazz(), id);
        if (entityManager.isOpen()) {
            entityManager.close();
        }
        return obj;
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, JsonObject searchCondition, String... customCondition) {
        return eruptJpaDao.queryEruptList(eruptModel, page, searchCondition, customCondition);
    }

    @Override
    public List<TreeModel> queryTree(EruptModel eruptModel, String... customCondition) {
        return treeDataUtil(eruptModel, null, customCondition);
    }

    private List<TreeModel> treeDataUtil(EruptModel eruptModel, String sort, String... condition) {
        Tree tree = eruptModel.getErupt().tree();
        List<String> cols = new ArrayList<>();
        cols.add(EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), tree.id()) + " as " + AnnotationConst.ID);
        cols.add(EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), tree.label()) + " as " + AnnotationConst.LABEL);
        if (StringUtils.isNotBlank(tree.pid())) {
            String pid = EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), tree.pid());
            String expr = AnnotationUtil.getExpr(tree.rootTagExpr());
            cols.add(pid + " as " + AnnotationConst.PID);
            if (StringUtils.isNotBlank(expr)) {
                cols.add(String.format("(%s) as ", expr) + AnnotationConst.ROOT);
            } else {
                cols.add(pid + " as " + AnnotationConst.ROOT);
            }
        }
        List<Map<String, Object>> list = eruptJpaDao.getDataMap(eruptModel, cols, sort, condition);
        List<TreeModel> treeModels = new ArrayList<>();
        for (Map map : list) {
            TreeModel treeModel = new TreeModel(map.get(AnnotationConst.ID), map.get(AnnotationConst.LABEL), map.get(AnnotationConst.PID), null, map.get(AnnotationConst.ROOT));
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
            eruptJpaDao.addEntity(eruptModel, object);
        } catch (Exception e) {
            handlerException(e, eruptModel);
        }
    }

    @Transactional
    @Override
    public void editData(EruptModel eruptModel, Object data) {
        try {
            eruptJpaDao.editEntity(eruptModel, data);
        } catch (Exception e) {
            handlerException(e, eruptModel);
        }
    }

    //优化异常提示类
    private void handlerException(Exception e, EruptModel eruptModel) {
        if (e instanceof DataIntegrityViolationException) {
            if (e.getMessage().contains("ConstraintViolationException")) {
                throw new EruptWebApiRuntimeException(gcRepeatHint(eruptModel));
            } else if (e.getMessage().contains("DataException")) {
                throw new EruptWebApiRuntimeException("内容超出数据库限制长度！");
            } else {
                throw new EruptWebApiRuntimeException(e.getMessage());
            }
        } else {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        try {
            eruptJpaDao.removeEntity(eruptModel, object);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            e.printStackTrace();
            throw new EruptWebApiRuntimeException("删除失败，可能存在关联数据，无法直接删除！");
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    //@ManyToOne数据处理
    private void jpaManyToOneConvert(EruptModel eruptModel, Object object) throws NoSuchFieldException, IllegalAccessException {
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            if (fieldModel.getEruptField().edit().type() == EditType.TAB_TABLE_ADD) {
                Field field = object.getClass().getDeclaredField(fieldModel.getFieldName());
                field.setAccessible(true);
                Collection collection = (Collection) field.get(object);
                if (null != collection) {
                    for (Object o : collection) {
                        //删除主键ID
                        //TODO 强制删除id的处理方式并不好
                        Field pk = ReflectUtil.findClassField(o.getClass(), EruptCoreService
                                .getErupt(fieldModel.getFieldReturnName()).getErupt().primaryKeyCol());
                        pk.set(o, null);
                    }
                }
            }
        }
    }

    //生成数据重复的提示字符串
    private String gcRepeatHint(EruptModel eruptModel) {
        StringBuilder str = new StringBuilder();
        for (UniqueConstraint uniqueConstraint : eruptModel.getClazz().getAnnotation(Table.class).uniqueConstraints()) {
            for (String columnName : uniqueConstraint.columnNames()) {
                EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(columnName);
                if (null != eruptFieldModel) {
                    str.append(eruptFieldModel.getEruptField().views()[0].title()).append("、");
                }
            }
        }
        String repeatTxt = "数据重复";
        if (StringUtils.isNotBlank(str)) {
            return str.substring(0, str.length() - 1) + repeatTxt;
        } else {
            return repeatTxt;
        }
    }

    @Override
    public List<TreeModel> findTabTree(EruptModel eruptModel, String fieldName) {
        EruptFieldModel eruptTabFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        EruptModel subEruptModel = EruptCoreService.getErupt(eruptModel.getEruptFieldMap().get(fieldName).getFieldReturnName());
        Filter[] filters = eruptTabFieldModel.getEruptField().edit().filter();
        String[] conditions = new String[filters.length];
        for (int i = 0; i < filters.length; i++) {
            conditions[i] = AnnotationUtil.switchFilterConditionToStr(filters[i]);
        }
        return treeDataUtil(subEruptModel, eruptTabFieldModel.getEruptField().edit().orderBy(), conditions);
    }


    @Override
    public Collection<TreeModel> getReferenceTree(EruptModel eruptModel, String fieldName, Serializable dependValue, String... conditionStr) {
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        Edit edit = eruptFieldModel.getEruptField().edit();
        ReferenceTreeType refTree = edit.referenceTreeType();
        List<String> cols = new ArrayList<>();
        cols.add(EruptJpaUtils.completeHqlPath(eruptFieldModel.getFieldReturnName(), refTree.id()) + " as " + AnnotationConst.ID);
        cols.add(EruptJpaUtils.completeHqlPath(eruptFieldModel.getFieldReturnName(), refTree.label()) + " as " + AnnotationConst.LABEL);
        if (StringUtils.isNotBlank(refTree.pid())) {
            String pid = EruptJpaUtils.completeHqlPath(eruptFieldModel.getFieldName(), refTree.pid());
            cols.add(pid + " as " + AnnotationConst.PID);
            String expr = AnnotationUtil.getExpr(refTree.rootTagExpr());
            if (StringUtils.isNotBlank(expr)) {
                cols.add(String.format("(%s) as ", expr) + AnnotationConst.ROOT);
            } else {
                cols.add(String.format("(%s is null) as ", pid) + AnnotationConst.ROOT);
            }
        }
        List<String> conditions = new ArrayList<>();
        for (Filter filter : edit.filter()) {
            String filterStr = AnnotationUtil.switchFilterConditionToStr(filter);
            if (StringUtils.isNotBlank(filterStr)) {
                conditions.add(filterStr);
            }
        }
        //处理depend参数代码
        if (StringUtils.isNotBlank(refTree.dependField()) && null != dependValue) {
            conditions.add(eruptFieldModel.getEruptField().edit().referenceTreeType().dependColumn() + String.format("='%s'", dependValue));
        }
        conditions.addAll(Arrays.asList(conditionStr));
        List<Map<String, Object>> list = eruptJpaDao.getDataMap(EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName())
                , cols, edit.orderBy(), conditions.toArray(new String[0]));
        List<TreeModel> treeModels = new ArrayList<>();
        for (Map<String, Object> map : list) {
            TreeModel treeModel = new TreeModel(map.get(AnnotationConst.ID), map.get(AnnotationConst.LABEL), map.get(AnnotationConst.PID), null, map.get(AnnotationConst.ROOT));
            treeModel.setRoot(map.get(AnnotationConst.ROOT));
            treeModels.add(treeModel);
        }
        if (StringUtils.isBlank(refTree.pid())) {
            return treeModels;
        } else {
            return DataHandlerUtil.treeModelToTree(treeModels);
        }
    }

    /**
     * 根据列获取相关数据
     *
     * @param eruptModel eruptModel
     * @param columns    列
     * @param query      查询对象
     * @return
     */
    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<String> columns, Query query) {
        StringBuilder hql = new StringBuilder();
        hql.append("select new map(").append(String.join(", ", columns)).append(") from ")
                .append(eruptModel.getEruptName()).append(" as ").append(eruptModel.getEruptName());

        ReflectUtil.findClassAllFields(eruptModel.getClazz(), field -> {
            if (null != field.getAnnotation(ManyToOne.class) || null != field.getAnnotation(OneToOne.class)) {
                hql.append(" left outer join ").append(eruptModel.getEruptName()).append(".")
                        .append(field.getName()).append(" as ").append(field.getName());
            }
        });
        hql.append(" where 1 = 1 ");
        if (null != query.getConditions()) {
            for (Condition condition : query.getConditions()) {
                hql.append(EruptJpaUtils.AND).append(condition.getKey()).append("=").append(condition.getValue());
            }
        }
        for (Filter filter : eruptModel.getErupt().filter()) {
            String filterStr = AnnotationUtil.switchFilterConditionToStr(filter);
            if (StringUtils.isNotBlank(filterStr)) {
                hql.append(EruptJpaUtils.AND).append(filterStr);
            }
        }
        if (null != query.getOrders()) {
            hql.append(" order by ");
            for (Order order : query.getOrders()) {
                hql.append(order.getProperties()).append(" ").append(order.getDirection().name()).append(",");
            }
            hql.substring(0, hql.length() - 1);
        }
        EntityManager entityManager = entityManagerService.getEntityManager(eruptModel);
        List list = entityManager.createQuery(hql.toString()).getResultList();
        if (entityManager.isOpen()) {
            entityManager.close();
        }
        return list;
    }
}
