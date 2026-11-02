package xyz.erupt.core.controller;

import xyz.erupt.core.scope.EruptScopeService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.model.Row;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.CheckboxType;
import xyz.erupt.annotation.sub_field.sub_edit.TransferType;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.naming.EruptRowOperationNaming;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.service.PreEruptDataService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.TypeUtil;
import xyz.erupt.core.view.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data query
 *
 * @order 6
 * @apiNote Read side of erupt data: table, tree, detail, reference pickers and row operations.
 *
 * @author YuePeng
 * date 9/28/18.
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_DATA)
@RequiredArgsConstructor
@Slf4j
public class EruptDataController {

    private final EruptService eruptService;

    private final PreEruptDataService preEruptDataService;

    private final EruptScopeService eruptScopeService;

    public static final int MAX_PAGE_SIZE = 50000;

    private final Gson gson = GsonFactory.getGson();

    /**
     * Query a page of table rows.
     *
     * @apiNote Conditions, sort and paging come from the body; DataProxy.beforeFetch/afterFetch and @Filter run on the server. pageSize is capped at 50000.
     * @param eruptName Simple class name of the @Erupt model
     * @param tableQuery Paging, sort, search conditions and the optional @Vis code
     * @return one page of rows as maps keyed by field name
     */
    @PostMapping({"/table/{erupt}"})
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, power = EruptRouter.Power.QUERY, payload = EruptRouter.Payload.TABLE_QUERY, resolve = EruptRouter.Resolve.PAGE)
    public Page getEruptData(@PathVariable("erupt") String eruptName, @RequestBody TableQuery tableQuery) {
        if (tableQuery.getPageSize() > MAX_PAGE_SIZE) {
            tableQuery.setPageSize(MAX_PAGE_SIZE);
        }
        return eruptService.getEruptData(EruptCoreService.getErupt(eruptName), tableQuery, null);
    }

    /**
     * Totals of the @View(statistic) columns.
     *
     * @apiNote Aggregates over every row the same query matches, not only the current page.
     * @param eruptName Simple class name of the @Erupt model
     * @param tableQuery Same query as the table request
     * @return column name to aggregated value
     */
    @PostMapping("/aggregate/{erupt}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, power = EruptRouter.Power.QUERY, payload = EruptRouter.Payload.TABLE_QUERY)
    public Map<String, Object> getEruptAggregate(@PathVariable("erupt") String eruptName, @RequestBody TableQuery tableQuery) {
        return eruptService.getEruptAggregate(EruptCoreService.getErupt(eruptName), tableQuery);
    }

    /**
     * Whole tree of a @Tree erupt.
     *
     * @param eruptName Simple class name of the @Erupt model
     * @return nested tree nodes
     */
    @GetMapping("/tree/{erupt}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, power = EruptRouter.Power.QUERY, resolve = EruptRouter.Resolve.TREE)
    public Collection<TreeModel> getEruptTreeData(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, PowerObject::isQuery);
        Tree tree = eruptModel.getErupt().tree();
        return preEruptDataService.geneTree(eruptModel, tree.id(), tree.label(), tree.pid(), tree.rootPid(),
                EruptQuery.builder().conditions(eruptScopeService.conditions(eruptModel)).build());
    }

    /**
     * Initial values for a new record form.
     *
     * @apiNote A fresh instance after DataProxy.addBehavior, so defaults set there reach the form.
     * @param eruptName Simple class name of the @Erupt model
     * @return field name to initial value
     */
    @GetMapping("/init-value/{erupt}")
    @SneakyThrows
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, power = EruptRouter.Power.ADD)
    public Map<String, Object> initEruptValue(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Object behavior = eruptModel.getClazz().getDeclaredConstructor().newInstance();
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.addBehavior(behavior)));
        Object obj = behavior;
        if (!EruptScopeService.boundFields(eruptModel).isEmpty()) {
            JsonObject json = gson.toJsonTree(behavior).getAsJsonObject();
            eruptScopeService.prefill(eruptModel, json);
            obj = gson.fromJson(json, eruptModel.getClazz());
        }
        return EruptUtil.generateEruptDataMap(eruptModel, obj, false);
    }

    /**
     * One record by primary key.
     *
     * @apiNote Requires edit or view-details permission. On an erupt-cloud server a remote erupt is forwarded to the node that owns it.
     * @param eruptName Simple class name of the @Erupt model
     * @param id Primary key
     * @return record as a map keyed by field name
     */
    @GetMapping("/{erupt}/{id}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, power = {EruptRouter.Power.EDIT, EruptRouter.Power.VIEW_DETAILS}, resolve = EruptRouter.Resolve.RECORD)
    public Map<String, Object> getEruptDataById(@PathVariable("erupt") String eruptName, @PathVariable("id") String id) {
        // Remote resolution: called in-process by AI tools (findEruptDataByPk) with node erupt names
        EruptModel eruptModel = EruptCoreService.getEruptWithRemote(eruptName);
        // erupt-cloud: forward to the owning node, which enforces its own permission checks
        if (eruptModel.isRemote()) {
            return EruptRemoteRouterManager.get().findById(eruptName, id);
        }
        Erupts.powerLegal(eruptModel, powerObject -> powerObject.isEdit() || powerObject.isViewDetails());
        eruptService.verifyIdPermissions(eruptModel, id);
        return preEruptDataService.getEruptData(eruptModel, id, false);
    }

    public static final String OPERATOR_PATH_STR = "/operator";

    /**
     * Initial form values of a row operation.
     *
     * @apiNote For a @RowOperation that opens an erupt form: builds the form object, runs addBehavior and, when the OperationHandler overrides eruptFormValue, lets it prefill from the selected rows.
     * @param eruptName Simple class name of the @Erupt model
     * @param code @RowOperation.code
     * @param ids Primary keys of the selected rows
     * @return form object, or null when no handler is configured
     */
    @PostMapping("/{erupt}" + OPERATOR_PATH_STR + "/{code}/form-value")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.OPERATION_FORM)
    @SneakyThrows
    public Object eruptOperatorFormValue(@PathVariable("erupt") String eruptName, @PathVariable("code") String code, @RequestParam(value = "ids", required = false) List<Object> ids) {
        if (null == ids) ids = new ArrayList<>();
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        RowOperation rowOperation = Arrays.stream(eruptModel.getErupt().rowOperation()).filter(it -> code.equals(it.code())).findFirst().orElseThrow(EruptNoLegalPowerException::new);
        EruptModel operatorErupt = EruptCoreService.getErupt(rowOperation.eruptClass().getSimpleName());
        if (rowOperation.operationHandler().isInterface()) return null;
        this.verifyScope(eruptModel, ids.stream().map(Object::toString).toList());
        OperationHandler<Object, Object> operationHandler = EruptSpringUtil.getBean(rowOperation.operationHandler());
        Object eruptForm = rowOperation.eruptClass().getDeclaredConstructor().newInstance();
        DataProxyInvoke.invoke(operatorErupt, (dataProxy -> dataProxy.addBehavior(eruptForm)));
        try {
            operationHandler.getClass().getDeclaredMethod("eruptFormValue", List.class, operatorErupt.getClazz(), String[].class);
            return operationHandler.eruptFormValue(ids.stream().map(id -> DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                    .findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id.toString()))).collect(Collectors.toList()), eruptForm, rowOperation.operationParam());
        } catch (NoSuchMethodException ignored) {
        }
        return eruptForm;
    }

    /**
     * Execute a row operation.
     *
     * @apiNote Body is {"ids": [...], "param": {...}}: ids are the selected rows, param the operation form values (validated against the operation erupt). The handler may return a script for the frontend to evaluate; it comes back in data.
     * @param eruptName Simple class name of the @Erupt model
     * @param code @RowOperation.code
     * @param body ids and param
     * @return result; data holds the handler script when there is one
     */
    @PostMapping("/{erupt}" + OPERATOR_PATH_STR + "/{code}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.OPERATION)
    @EruptRecordOperate(value = "", dynamicConfig = EruptRowOperationNaming.class)
    public R<?> eruptOperatorExec(@PathVariable("erupt") String eruptName, @PathVariable("code") String code, @RequestBody JsonObject body) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        RowOperation rowOperation = Arrays.stream(eruptModel.getErupt().rowOperation()).filter(it -> code.equals(it.code())).findFirst().orElseThrow(EruptNoLegalPowerException::new);
        Erupts.powerLegal(ExprInvoke.getExpr(rowOperation.show()));
        if (rowOperation.eruptClass() != void.class) {
            EruptModel erupt = EruptCoreService.getErupt(rowOperation.eruptClass().getSimpleName());
            R<Void> validation = EruptUtil.validateEruptValue(erupt, body.getAsJsonObject("param"));
            if (!validation.isSuccess()) return validation;
        }
        if (rowOperation.operationHandler().isInterface()) {
            return R.errorDialog("Please implement the 'OperationHandler' interface for " + rowOperation.title());
        }
        OperationHandler<Object, Object> operationHandler = EruptSpringUtil.getBean(rowOperation.operationHandler());
        Object param = null;
        if (!body.get("param").isJsonNull()) {
            param = gson.fromJson(body.getAsJsonObject("param"), rowOperation.eruptClass());
        }
        if (rowOperation.mode() == RowOperation.Mode.BUTTON) {
            String eval = operationHandler.exec(null, param, rowOperation.operationParam());
            if (StringUtils.isNotBlank(eval)) {
                return R.success(eval);
            } else {
                return R.success(I18nTranslate.$translate("erupt.exec_success"), null);
            }
        }
        if (body.get("ids").isJsonArray() && !body.getAsJsonArray("ids").isEmpty()) {
            this.verifyScope(eruptModel, body.getAsJsonArray("ids").asList().stream().map(JsonElement::getAsString).toList());
            List<Object> list = new ArrayList<>();
            body.getAsJsonArray("ids").forEach(id -> list.add(DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                    .findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id.getAsString()))));
            String eval = operationHandler.exec(list, param, rowOperation.operationParam());
            if (StringUtils.isNotBlank(eval)) {
                return R.success(eval);
            }
        }
        return R.success(I18nTranslate.$translate("erupt.exec_success"), null);
    }

    /**
     * Tree options of a TAB_TREE field.
     *
     * @param eruptName Simple class name of the @Erupt model
     * @param tabFieldName Field name with edit type TAB_TREE
     * @return nested tree nodes
     */
    @GetMapping("/tab/tree/{erupt}/{tabFieldName}")
    @EruptRouter(authIndex = 3, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.TAB_TREE)
    public Collection<TreeModel> findTabTree(@PathVariable("erupt") String eruptName, @PathVariable("tabFieldName") String tabFieldName) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
//        Erupts.powerLegal(eruptModel, powerObject -> powerObject.isViewDetails() || powerObject.isEdit());
        EruptModel tabEruptModel = EruptCoreService.getErupt(eruptModel.getEruptFieldMap().get(tabFieldName).getFieldReturnName());
        Tree tree = tabEruptModel.getErupt().tree();
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(tabFieldName);
        EruptQuery eruptQuery = EruptQuery.builder().conditionStrings(
                Arrays.stream(eruptFieldModel.getEruptField().edit().filter()).map(Filter::value).collect(Collectors.toList())
        ).build();
        return preEruptDataService.geneTree(tabEruptModel, tree.id(), tree.label(), tree.pid(), tree.rootPid(), eruptQuery);
    }

    /**
     * Options of a CHECKBOX or TRANSFER field.
     *
     * @param eruptName Simple class name of the @Erupt model
     * @param fieldName Field name with edit type CHECKBOX or TRANSFER
     * @return id, label and remark of every option
     */
    @GetMapping("/{erupt}/checkbox/{fieldName}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.CHECKBOX)
    public Collection<CheckboxModel<Object, Object, Object>> findCheckbox(@PathVariable("erupt") String eruptName, @PathVariable("fieldName") String fieldName) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        EruptModel tabEruptModel = EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName());
        Edit edit = eruptFieldModel.getEruptField().edit();
        // CHECKBOX and TRANSFER share the option shape, only the widget differs
        String id, label, remark;
        if (edit.type() == EditType.TRANSFER) {
            TransferType transferType = edit.transferType();
            id = transferType.id();
            label = transferType.label();
            remark = transferType.remark();
        } else {
            CheckboxType checkboxType = edit.checkboxType();
            id = checkboxType.id();
            label = checkboxType.label();
            remark = checkboxType.remark();
        }
        List<Column> columns = new ArrayList<>();
        columns.add(new Column(id, AnnotationConst.ID));
        columns.add(new Column(label, AnnotationConst.LABEL));
        if (!AnnotationConst.EMPTY_STR.equals(remark)) {
            columns.add(new Column(remark, AnnotationConst.REMARK));
        }
        EruptQuery eruptQuery = EruptQuery.builder().conditionStrings(
                Arrays.stream(eruptFieldModel.getEruptField().edit().filter()).map(Filter::value).collect(Collectors.toList())
        ).build();
        Collection<Map<String, Object>> collection = preEruptDataService.createColumnQuery(tabEruptModel, columns, eruptQuery);
        Collection<CheckboxModel<Object, Object, Object>> checkboxModels = new ArrayList<>(collection.size());
        collection.forEach(map -> checkboxModels.add(new CheckboxModel<>(map.get(AnnotationConst.ID), map.get(AnnotationConst.LABEL), map.get(AnnotationConst.REMARK))));
        return checkboxModels;
    }

    /**
     * Pick list of a REFERENCE_TABLE field.
     *
     * @apiNote Pages through the referenced erupt with the same query shape as the table request. When @ReferenceTableType.dependField is set the current value of that field must be sent as dependValue.
     * @param eruptName Simple class name of the @Erupt model
     * @param fieldName Field name with edit type REFERENCE_TABLE
     * @param dependValue Value of the depend field, required when one is configured
     * @param tabRef true when called from a TAB_TABLE_REFER field
     * @param tableQuery Paging, sort and search conditions
     * @return one page of referenced rows
     */
    @PostMapping("/{erupt}/reference-table/{fieldName}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.REFERENCE_TABLE)
    public Page getReferenceTable(@PathVariable("erupt") String eruptName,
                                  @PathVariable("fieldName") String fieldName,
                                  @RequestParam(value = "dependValue", required = false) Serializable dependValue,
                                  @RequestParam(value = "tabRef", required = false) Boolean tabRef,
                                  @RequestBody TableQuery tableQuery) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        Edit edit = eruptFieldModel.getEruptField().edit();
        String dependField = edit.referenceTableType().dependField();
        List<Condition> serverConditions = new ArrayList<>();
        List<String> conditions = Arrays.stream(edit.filter()).map(Filter::value).toList();
        if (!AnnotationConst.EMPTY_STR.equals(dependField)) {
            Erupts.requireNonNull(dependValue, I18nTranslate.$translate("erupt.select") + " " + eruptModel.getEruptFieldMap().get(dependField).getEruptField().edit().title());
            EruptModel refErupt = EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName());
            serverConditions.add(new Condition(
                    eruptFieldModel.getFieldReturnName() + EruptConst.DOT + edit.referenceTableType().dependColumn(),
                    TypeUtil.typeStrConvertObject(dependValue, refErupt.getEruptFieldMap().get(refErupt.getErupt().primaryKeyCol()).getField().getType()),
                    QueryExpression.EQ
            ));
        }
        EruptModel eruptReferenceModel = EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName());
        if (!tabRef) {
            // Due to class loading order issues, validation is not performed at startup
            ReferenceTableType referenceTableType = eruptFieldModel.getEruptField().edit().referenceTableType();
            Erupts.requireTrue(eruptReferenceModel.getEruptFieldMap().containsKey(referenceTableType.label().split("\\.")[0])
                    , eruptReferenceModel.getEruptName() + " not found '" + referenceTableType.label()
                            + "' field，please use @ReferenceTableType annotation 'label' config");
        }
        return eruptService.getEruptData(eruptReferenceModel, tableQuery,
                serverConditions, conditions.toArray(new String[0]));
    }

    /**
     * Tree of the @LinkTree field.
     *
     * @apiNote The left-hand tree shown next to a table; selecting a node filters the table.
     * @param erupt Simple class name of the @Erupt model
     * @return nested tree nodes
     */
    @SneakyThrows
    @GetMapping("/depend-tree/{erupt}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.LINK_TREE)
    public Collection<TreeModel> getDependTree(@PathVariable("erupt") String erupt) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        String field = eruptModel.getErupt().linkTree().field();
        if (null == eruptModel.getEruptFieldMap().get(field)) {
            String treeErupt = eruptModel.getClazz().getDeclaredField(field).getType().getSimpleName();
            return this.getEruptTreeData(treeErupt);
        }
        return this.getReferenceTree(eruptModel.getEruptName(), field, null);
    }

    /**
     * Pick tree of a REFERENCE_TREE field.
     *
     * @param erupt Simple class name of the @Erupt model
     * @param fieldName Field name with edit type REFERENCE_TREE
     * @param dependValue Value of the depend field, required when one is configured
     * @return nested tree nodes
     */
    @GetMapping("/{erupt}/reference-tree/{fieldName}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.REFERENCE_TREE)
    public Collection<TreeModel> getReferenceTree(@PathVariable("erupt") String erupt,
                                                  @PathVariable("fieldName") String fieldName,
                                                  @RequestParam(value = "dependValue", required = false) Serializable dependValue) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        String dependField = eruptFieldModel.getEruptField().edit().referenceTreeType().dependField();
        if (!AnnotationConst.EMPTY_STR.equals(dependField)) {
            Erupts.requireNonNull(dependValue, I18nTranslate.$translate("erupt.select") + " " + eruptModel.getEruptFieldMap().get(dependField).getEruptField().edit().title());
        }
        Edit edit = eruptFieldModel.getEruptField().edit();
        ReferenceTreeType treeType = edit.referenceTreeType();
        EruptModel referenceEruptModel = EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName());
        Erupts.requireTrue(referenceEruptModel.getEruptFieldMap().containsKey(treeType.label().split("\\.")[0]),
                referenceEruptModel.getEruptName() + " not found " + treeType.label() + " field, please use @ReferenceTreeType annotation config");
        List<Condition> conditions = new ArrayList<>();
        // process depend
        if (StringUtils.isNotBlank(treeType.dependField()) && null != dependValue) {
            conditions.add(new Condition(edit.referenceTreeType().dependColumn(), dependValue, QueryExpression.EQ));
        }
        List<String> conditionStrings = Arrays.stream(edit.filter()).map(Filter::value).collect(Collectors.toList());
        return preEruptDataService.geneTree(referenceEruptModel, treeType.id(), treeType.label(), treeType.pid(), treeType.rootPid(),
                EruptQuery.builder().sort(Sort.toSortList(edit.orderBy())).conditionStrings(conditionStrings).conditions(conditions).build());
    }

    /**
     * Extra rows appended to the table.
     *
     * @apiNote Rows produced by DataProxy.extraRow for the current search conditions, such as a summary line.
     * @param erupt Simple class name of the @Erupt model
     * @param tableQuery Current search conditions
     * @return extra rows
     */
    @PostMapping("/extra-row/{erupt}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, power = EruptRouter.Power.QUERY, payload = EruptRouter.Payload.TABLE_QUERY)
    public List<Row> extraRow(@PathVariable("erupt") String erupt, @RequestBody TableQuery tableQuery) {
        List<Row> rows = new ArrayList<>();
        DataProxyInvoke.invoke(EruptCoreService.getErupt(erupt), dataProxy ->
                Optional.ofNullable(dataProxy.extraRow(tableQuery.getCondition())).ifPresent(rows::addAll));
        return rows;
    }

    /**
     * Server side onchange of a field.
     *
     * @apiNote Runs the @Edit.onchange handler with the whole form and returns the values and edit expressions it wants applied.
     * @param eruptName Simple class name of the @Erupt model
     * @param field Field whose value changed
     * @param data Current form values keyed by field name
     * @return values to populate and edit expressions to apply
     */
    @PostMapping("/onchange/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, resolve = EruptRouter.Resolve.ONCHANGE)
    public R<OnChangeVo> onchange(@PathVariable("erupt") String eruptName, @PathVariable("field") String field, @RequestBody JsonObject data) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Edit edit = eruptModel.getEruptFieldMap().get(field).getEruptField().edit();
        Object o = gson.fromJson(data.toString(), eruptModel.getClazz());
        OnChange<Object> onChange = EruptSpringUtil.getBean(edit.onchange());
        OnChangeVo onChangeVo = new OnChangeVo();
        onChangeVo.setFormData(onChange.populateForm(o, edit.onchangeParams()));
        onChangeVo.setEditExpr(onChange.buildEditExpr(o, edit.onchangeParams()));
        return R.ok(onChangeVo);
    }


    // A row operation on a scoped model only reaches rows inside the current scope
    private void verifyScope(EruptModel eruptModel, List<String> ids) {
        if (EruptScopeService.boundFields(eruptModel).isEmpty()) return;
        eruptService.verifyIdPermissions(eruptModel, ids);
    }

}