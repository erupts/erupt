package xyz.erupt.ai_claw.tool;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.controller.EruptDataController;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.R;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.upms.service.EruptUserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author YuePeng
 * date 2026/4/6 23:25
 */
@AiToolbox
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class EruptModelTools {

    @Resource
    private EruptService eruptService;

    @Resource
    private EruptDataController eruptDataController;

    @Resource
    private EruptModifyService eruptModifyService;

    @Resource
    private EruptUserService eruptUserService;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final int MAX_PAGE_SIZE = 200;

    // keys of the body the operation route reads
    private static final String OPERATION_IDS = "ids";

    private static final String OPERATION_PARAM = "param";

    public static final String ERUPT_NAME_PARAM_HINT = "Erupt model name (call eruptModelList first if unknown)";

    @Tool("List all loaded Erupt modules with their name and description. " +
            "Use this to understand what features and capabilities are available in the current deployment.")
    public String getEruptModules() {
        List<String> modules = new ArrayList<>();
        EruptModuleInvoke.invoke(m -> {
            var info = m.info();
            modules.add(info.getName() + (info.getDescription() != null ? ": " + info.getDescription() : ""));
        });
        return modules.isEmpty() ? "No modules loaded." : String.join("\n", modules);
    }

    @Tool("Erupt data model list")
    public String eruptModelList() {
        String token = requireToken();
        // a tenant's administrator lists what its package's menus offer, not every model of the deployment
        boolean superAdmin = requireLogin(token).isPlatformAdmin();
        // One Redis round trip: read all allowed menu values up front, then match in-memory.
        // Menu keys are stored lower-cased (see EruptTokenService#loginToken).
        Map<String, Boolean> allowedMenus = superAdmin ? null : eruptUserService.getEruptMenuValuesMap(token);
        StringBuilder sb = new StringBuilder();
        for (EruptModel erupt : EruptCoreService.getErupts()) {
            if (!erupt.getErupt().power().ai()) continue;
            if (superAdmin || allowedMenus.containsKey(erupt.getEruptName().toLowerCase())) {
                sb.append(erupt.getEruptName()).append(": ").append(erupt.getErupt().name()).append("\n");
            }
        }
        // erupt-cloud: include erupts served by remote nodes (call eruptSchema to inspect their fields).
        // Remote erupts have no local annotation — access-control gating is deferred to the owning node.
        if (null != EruptRemoteRouterManager.get()) {
            for (String name : EruptRemoteRouterManager.get().remoteEruptNames()) {
                if (superAdmin || allowedMenus.containsKey(name.toLowerCase())) {
                    sb.append(name).append("\n");
                }
            }
        }
        return sb.toString();
    }

    @Tool("Erupt data model schema. If the erupt model name is not specified, call eruptModelList first to get available model names.")
    public String eruptSchema(@P(ERUPT_NAME_PARAM_HINT) String eruptName) {
        checkErupt(eruptName, PowerObject::isQuery);
        EruptModel erupt = EruptCoreService.getEruptView(eruptName);
        return GsonFactory.getGson().toJson(erupt);
    }

    @Tool("Query erupt model data with structured filter conditions, sort and pagination. " +
            "Call eruptSchema first to discover field names. Returns a Page with pageIndex/pageSize/total/list.")
    public String eruptDataQuery(
            @P(ERUPT_NAME_PARAM_HINT) String eruptName,
            @P("Filter conditions. Each Condition has: key (field name), expression (EQ/NEQ/GT/GTE/LT/LTE/LIKE/NOT_LIKE/IN/NOT_IN/RANGE/NULL/NOT_NULL), value (filter value; array for IN/NOT_IN/RANGE). Null or empty for no filter.")
            List<Condition> conditions,
            @P("Sort orders. Each Sort has: field (field name), direction (ASC/DESC). Null or empty for default order.")
            List<Sort> sort,
            @P("Page index, 1-based. Defaults to 1.") Integer pageIndex,
            @P("Page size, defaults to 20, max 200.") Integer pageSize) {
        checkErupt(eruptName, PowerObject::isQuery);
        TableQuery tableQuery = new TableQuery();
        tableQuery.setPageIndex(null == pageIndex || pageIndex < 1 ? 1 : pageIndex);
        tableQuery.setPageSize(null == pageSize || pageSize < 1
                ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE));
        tableQuery.setCondition(null == conditions ? new ArrayList<>() : conditions);
        tableQuery.setSort(sort);
        Page result = eruptService.getEruptData(EruptCoreService.getEruptWithRemote(eruptName), tableQuery, null);
        return GsonFactory.getGson().toJson(result);
    }

    @Tool("Insert a new record into the specified erupt model. Call eruptSchema first to ensure the data object contains all required fields with correct types.")
    public String insertEruptData(
            @P(ERUPT_NAME_PARAM_HINT) String eruptName,
            @P("JSON object representing the new record. Field names and types must match the model schema obtained from eruptSchema.") Map<String, Object> data) {
        checkErupt(eruptName, PowerObject::isAdd);
        JsonObject jsonObject = GsonFactory.getGson().toJsonTree(data).getAsJsonObject();
        return "Insert success, Primary key:" + eruptModifyService.insertEruptData(EruptCoreService.getEruptWithRemote(eruptName), jsonObject);
    }

    @Tool("Fetch a single erupt model record by its primary key ID.")
    public String findEruptDataByPk(
            @P(ERUPT_NAME_PARAM_HINT) String eruptName,
            @P("Primary key value of the record to retrieve") String id) {
        checkErupt(eruptName, PowerObject::isQuery);
        return GsonFactory.getGson().toJson(eruptDataController.getEruptDataById(eruptName, id));
    }


    @Tool("Update an existing record in the specified erupt model. It is strongly recommended to call findEruptDataByPk first to retrieve the current record, then modify only the necessary fields before submitting the update. The data object must include the primary key field.")
    public String updateEruptData(
            @P(ERUPT_NAME_PARAM_HINT) String eruptName,
            @P("JSON object representing the updated record. Must include the primary key field. Obtain the full record via findEruptDataByPk first to avoid overwriting fields with null or incorrect values.") Map<String, Object> data) {
        checkErupt(eruptName, PowerObject::isEdit);
        JsonObject jsonObject = GsonFactory.getGson().toJsonTree(data).getAsJsonObject();
        eruptModifyService.updateEruptData(EruptCoreService.getEruptWithRemote(eruptName), jsonObject);
        return "success";
    }

    @Tool("Delete one or more records from the specified erupt model by their primary key IDs.")
    public String deleteEruptData(
            @P(ERUPT_NAME_PARAM_HINT) String eruptName,
            @P("List of primary key values identifying the records to delete. Use findEruptDataByPk or eruptDataQuery to confirm IDs before deletion.") List<Object> ids) {
        checkErupt(eruptName, PowerObject::isDelete);
        eruptModifyService.deleteEruptData(EruptCoreService.getEruptWithRemote(eruptName), ids, false);
        return "success";
    }

    @Tool("List the row operations of an erupt model: its business actions, such as approve or ship, which plain " +
            "insert/update/delete cannot perform. Each has a code, a mode (SINGLE: exactly one row, MULTI and MULTI_ONLY: " +
            "one or more rows, BUTTON: no row), an optional condition a row must meet (ifExpr, JavaScript over `item`), " +
            "an optional confirmation prompt and the fields of the form it asks for.")
    public String eruptOperations(@P(ERUPT_NAME_PARAM_HINT) String eruptName) {
        return GsonFactory.getGson().toJson(Arrays.stream(this.operable(eruptName).getErupt().rowOperation())
                .filter(EruptModelTools::callable).map(Operation::of).toList());
    }

    @Tool("Run a row operation of an erupt model. This performs a real business action: call eruptOperations first, " +
            "pass the rows its mode asks for, check its ifExpr against each row, and when it carries a confirmation " +
            "prompt ask the user before running it.")
    public String invokeEruptOperation(
            @P(ERUPT_NAME_PARAM_HINT) String eruptName,
            @P("Operation code, from eruptOperations") String code,
            @P(value = "Primary keys of the rows to act on; omit for a BUTTON operation", required = false) List<String> ids,
            @P(value = "Form values, for an operation that asks for a form", required = false) Map<String, Object> form) {
        RowOperation operation = Arrays.stream(this.operable(eruptName).getErupt().rowOperation())
                .filter(it -> it.code().equals(code)).filter(EruptModelTools::callable).findFirst()
                .orElseThrow(() -> new EruptWebApiRuntimeException("No such row operation: " + code));
        int rows = null == ids ? 0 : ids.size();
        // the route answers success without running anything when a row operation is sent no row
        Erupts.requireTrue(switch (operation.mode()) {
            case BUTTON -> true;
            case SINGLE -> rows == 1;
            case MULTI, MULTI_ONLY -> rows >= 1;
        }, "Operation " + code + " is " + operation.mode() + ", it was given " + rows + " row(s)");
        JsonObject body = new JsonObject();
        body.add(OPERATION_IDS, GsonFactory.getGson().toJsonTree(operation.mode() == RowOperation.Mode.BUTTON || null == ids ? List.of() : ids));
        body.add(OPERATION_PARAM, void.class == operation.eruptClass() ? JsonNull.INSTANCE
                : GsonFactory.getGson().toJsonTree(null == form ? Map.of() : form));
        R<?> result = eruptDataController.eruptOperatorExec(eruptName, code, body);
        Erupts.requireTrue(result.isSuccess(), result.getMessage());
        return StringUtils.defaultIfBlank(result.getMessage(), "success");
    }

    // What the page would offer as a button that reaches a handler: a TPL operation only renders a template
    private static boolean callable(RowOperation operation) {
        return RowOperation.Type.ERUPT == operation.type() && !operation.operationHandler().isInterface()
                && ExprInvoke.getExpr(operation.show());
    }

    // The operation route asks for the model's menu and nothing more, so no single power is checked here.
    // An operation of a cloud node model runs on its node, behind the node's own handler: not reachable from here
    private EruptModel operable(String eruptName) {
        this.checkErupt(eruptName, power -> true);
        EruptModel model = EruptCoreService.getEruptWithRemote(eruptName);
        Erupts.requireTrue(null != model && !model.isRemote(), "Row operations of a cloud node model are not available to AI tools: " + eruptName);
        return model;
    }

    private record Operation(String code, String title, String tip, RowOperation.Mode mode, String ifExpr,
                             String confirm, List<FormField> form) {

        static Operation of(RowOperation operation) {
            return new Operation(operation.code(), operation.title(), StringUtils.trimToNull(operation.tip()), operation.mode(),
                    StringUtils.trimToNull(operation.ifExpr()),
                    StringUtils.isBlank(operation.callHint()) ? null : I18nTranslate.$translate(operation.callHint()),
                    void.class == operation.eruptClass() ? null
                            : EruptCoreService.getErupt(operation.eruptClass().getSimpleName()).getEruptFieldModels().stream()
                            .filter(it -> StringUtils.isNotBlank(it.getEruptField().edit().title())).map(FormField::of).toList());
        }
    }

    private record FormField(String name, String title, String type, boolean required) {

        static FormField of(EruptFieldModel field) {
            Edit edit = field.getEruptField().edit();
            return new FormField(field.getFieldName(), edit.title(), edit.type().name(), edit.notNull());
        }
    }

    // Enforce menu access + per-erupt power for the current user. Platform admins bypass user checks,
    // but the model-level power.ai() opt-out still blocks AI usage regardless of role.
    // Remote erupts skip the local power check — the owning node runs its own permission pipeline.
    private void checkErupt(String eruptName, Function<PowerObject, Boolean> powerCheck) {
        String token = requireToken();
        MetaUserinfo user = requireLogin(token);
        // Downstream permission handlers (e.g. UpmsPowerHandler) resolve the target erupt
        // from MetaContext; on AI threads it still holds the /ai/chat request meta, so
        // re-register it with the model being accessed (mirrors EruptSecurityInterceptor)
        MetaContext.register(new MetaErupt(eruptName, eruptName));
        EruptModel eruptModel = EruptCoreService.getEruptWithRemote(eruptName);
        if (null != eruptModel && !eruptModel.isRemote() && !eruptModel.getErupt().power().ai()) {
            throw new EruptWebApiRuntimeException("AI access is disabled for this Erupt model: " + eruptName);
        }
        if (user.isPlatformAdmin()) return;
        if (null == eruptUserService.getEruptMenuByValue(eruptName, token)) {
            throw new EruptWebApiRuntimeException("Current user has no access to this Erupt model: " + eruptName);
        }
        if (null == eruptModel || eruptModel.isRemote()) return;
        Erupts.powerLegal(eruptModel, powerCheck);
    }

    // Tools run on langchain4j's ForkJoinPool workers where no HttpServletRequest is bound;
    // pull identity from MetaContext (propagated by LlmCore.streamingChat) instead of the request scope
    private String requireToken() {
        String token = MetaContext.getToken();
        if (null == token || token.isBlank()) throw new EruptWebApiRuntimeException("Login required");
        return token;
    }

    private MetaUserinfo requireLogin(String token) {
        MetaUserinfo user = eruptUserService.getSimpleUserInfoByToken(token);
        if (null == user) throw new EruptWebApiRuntimeException("Login required");
        return user;
    }

}
