package xyz.erupt.ai_claw.tool;

import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.controller.EruptDataController;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.upms.service.EruptUserService;

import java.util.ArrayList;
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
        boolean superAdmin = requireLogin().isSuperAdmin();
        StringBuilder sb = new StringBuilder();
        for (EruptModel erupt : EruptCoreService.getErupts()) {
            if (superAdmin || eruptUserService.getEruptMenuByValue(erupt.getEruptName()) != null) {
                sb.append(erupt.getEruptName()).append(": ").append(erupt.getErupt().name()).append("\n");
            }
        }
        // erupt-cloud: include erupts served by remote nodes (call eruptSchema to inspect their fields)
        if (null != EruptRemoteRouterManager.get()) {
            for (String name : EruptRemoteRouterManager.get().remoteEruptNames()) {
                if (superAdmin || eruptUserService.getEruptMenuByValue(name) != null) {
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
        Page result = eruptService.getEruptData(EruptCoreService.getErupt(eruptName), tableQuery, null);
        return GsonFactory.getGson().toJson(result);
    }

    @Tool("Insert a new record into the specified erupt model. Call eruptSchema first to ensure the data object contains all required fields with correct types.")
    public String insertEruptData(
            @P(ERUPT_NAME_PARAM_HINT) String eruptName,
            @P("JSON object representing the new record. Field names and types must match the model schema obtained from eruptSchema.") Map<String, Object> data) {
        checkErupt(eruptName, PowerObject::isAdd);
        JsonObject jsonObject = GsonFactory.getGson().toJsonTree(data).getAsJsonObject();
        return "Insert success, Primary key:" + eruptModifyService.insertEruptData(EruptCoreService.getErupt(eruptName), jsonObject);
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
        eruptModifyService.updateEruptData(EruptCoreService.getErupt(eruptName), jsonObject);
        return "success";
    }

    @Tool("Delete one or more records from the specified erupt model by their primary key IDs.")
    public String deleteEruptData(
            @P(ERUPT_NAME_PARAM_HINT) String eruptName,
            @P("List of primary key values identifying the records to delete. Use findEruptDataByPk or eruptDataQuery to confirm IDs before deletion.") List<Object> ids) {
        checkErupt(eruptName, PowerObject::isDelete);
        eruptModifyService.deleteEruptData(EruptCoreService.getErupt(eruptName), ids, false);
        return "success";
    }

    @Tool("Generate erupt annotation code. Returns the erupt annotation reference documentation to guide code generation.")
    public String geneEruptCode() throws Exception {
        try (var in = getClass().getClassLoader().getResourceAsStream("erupt-annotation.md")) {
            return new String(in.readAllBytes());
        }
    }

    // Enforce menu access + per-erupt power for the current user. Super admins bypass.
    // Remote erupts skip the local power check — the owning node runs its own permission pipeline.
    private void checkErupt(String eruptName, Function<PowerObject, Boolean> powerCheck) {
        MetaUserinfo user = requireLogin();
        if (user.isSuperAdmin()) return;
        if (null == eruptUserService.getEruptMenuByValue(eruptName)) {
            throw new EruptWebApiRuntimeException("Current user has no access to this Erupt model: " + eruptName);
        }
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        if (null == eruptModel || eruptModel.isRemote()) return;
        Erupts.powerLegal(eruptModel, powerCheck);
    }

    private MetaUserinfo requireLogin() {
        MetaUserinfo user = eruptUserService.getSimpleUserInfo();
        if (null == user) {
            throw new EruptWebApiRuntimeException("Login required");
        }
        return user;
    }

}
