package xyz.erupt.core.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.expr.Expr;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.Condition;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.DataHandlerUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.TreeModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class PreEruptDataService {

    /**
     * 根据要素生成树结构
     *
     * @param eruptModel eruptModel
     * @param id         id
     * @param label      label
     * @param pid        parent id
     * @param query      查询对象
     * @return 树对象
     */
    public Collection<TreeModel> geneTree(EruptModel eruptModel, String id, String label, String pid, Expr rootId, EruptQuery query) {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column(id, AnnotationConst.ID));
        columns.add(new Column(label, AnnotationConst.LABEL));
        if (!AnnotationConst.EMPTY_STR.equals(pid)) {
            columns.add(new Column(pid, AnnotationConst.PID));
        }
        Collection<Map<String, Object>> result = this.createColumnQuery(eruptModel, columns, query);
        String root = ExprInvoke.getExpr(rootId);
        List<TreeModel> treeModels = new ArrayList<>();
        for (Map<String, Object> map : result) {
            TreeModel treeModel = new TreeModel(map.get(AnnotationConst.ID), map.get(AnnotationConst.LABEL), map.get(AnnotationConst.PID), root);
            treeModels.add(treeModel);
        }
        if (StringUtils.isBlank(pid)) {
            return treeModels;
        } else {
            return DataHandlerUtil.treeModelToTree(treeModels);
        }
    }

    public Collection<Map<String, Object>> createColumnQuery(EruptModel eruptModel, List<Column> columns, EruptQuery query) {
        List<Condition> conditions = new ArrayList<>();
        List<String> conditionStrings = new ArrayList<>();
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> {
            String condition = dataProxy.beforeFetch(eruptModel.getClazz());
            if (StringUtils.isNotBlank(condition)) {
                conditionStrings.add(condition);
            }
        }));
        for (Filter filter : eruptModel.getErupt().filter()) {
            String filterStr = AnnotationUtil.switchFilterConditionToStr(filter);
            if (StringUtils.isNotBlank(filterStr)) {
                conditionStrings.add(filterStr);
            }
        }
        if (null != query.getConditions()) {
            conditions = query.getConditions();
        }
        if (null != query.getConditionStrings()) {
            conditionStrings.addAll(query.getConditionStrings());
        }
        String orderBy;
        if (StringUtils.isNotBlank(query.getOrderBy())) {
            orderBy = query.getOrderBy();
        } else {
            orderBy = eruptModel.getErupt().orderBy();
        }
        Collection<Map<String, Object>> result = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                .queryColumn(eruptModel, columns, EruptQuery.builder()
                        .conditions(conditions).conditionStrings(conditionStrings).orderBy(orderBy).build());
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterFetch(result)));
        return result;
    }

}
