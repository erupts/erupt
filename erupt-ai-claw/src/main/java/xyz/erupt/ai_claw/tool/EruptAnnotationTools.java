package xyz.erupt.ai_claw.tool;

import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiToolbox;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.controller.EruptDataController;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2026/4/6 23:25
 */
@AiToolbox
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class EruptAnnotationTools {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptDataController eruptDataController;

    @Resource
    private EruptModifyService eruptModifyService;

    public static final String ERUPT_NAME_PARAM_HINT = "Erupt model name (call eruptModelList first if unknown)";

    @Tool("Erupt data model list")
    public String eruptModelList() {
        StringBuilder sb = new StringBuilder();
        for (EruptModel erupt : EruptCoreService.getErupts()) {
            sb.append(erupt.getEruptName()).append(": ").append(erupt.getErupt().name()).append("\n");
        }
        return sb.toString();
    }

    @Tool("Erupt data model schema. If the erupt model name is not specified, call eruptModelList first to get available model names.")
    public String eruptSchema(@P(ERUPT_NAME_PARAM_HINT) String eruptName) {
        EruptModel erupt = EruptCoreService.getEruptView(eruptName);
        return GsonFactory.getGson().toJson(erupt);
    }

    @Tool("Query erupt model data. If the model structure is unknown, call eruptSchema first to get field definitions before writing HQL. Security restrictions: SELECT only (INSERT/UPDATE/DELETE/DROP/TRUNCATE are strictly forbidden).")
    public String eruptDataQuery(@P("HQL (Hibernate Query Language), SELECT only. Ensure model schema is known via eruptSchema before querying.") String hql) {
        List<?> result = eruptDao.getEntityManager().createQuery(hql)
                .setMaxResults(200)
                .getResultList();
        return GsonFactory.getGson().toJson(result);
    }

    @Transactional
    @Tool("Insert a new record into the specified erupt model. Call eruptSchema first to ensure the data object contains all required fields with correct types.")
    public String insertEruptData(
            @P(ERUPT_NAME_PARAM_HINT) String eruptName,
            @P("JSON object representing the new record. Field names and types must match the model schema obtained from eruptSchema.") Map<String, Object> data) {
        JsonObject jsonObject = GsonFactory.getGson().toJsonTree(data).getAsJsonObject();
        return GsonFactory.getGson().toJson(eruptModifyService.insertEruptData(EruptCoreService.getErupt(eruptName), jsonObject));
    }

    @Tool("Fetch a single erupt model record by its primary key ID.")
    public String findEruptDataByPk(
            @P(ERUPT_NAME_PARAM_HINT) String eruptName,
            @P("Primary key value of the record to retrieve") String id) {
        return GsonFactory.getGson().toJson(eruptDataController.getEruptDataById(eruptName, id));
    }


    @Transactional
    @Tool("Update an existing record in the specified erupt model. It is strongly recommended to call findEruptDataByPk first to retrieve the current record, then modify only the necessary fields before submitting the update. The data object must include the primary key field.")
    public String updateEruptData(
            @P(ERUPT_NAME_PARAM_HINT) String eruptName,
            @P("JSON object representing the updated record. Must include the primary key field. Obtain the full record via findEruptDataByPk first to avoid overwriting fields with null or incorrect values.") Map<String, Object> data) {
        JsonObject jsonObject = GsonFactory.getGson().toJsonTree(data).getAsJsonObject();
        eruptModifyService.updateEruptData(EruptCoreService.getErupt(eruptName), jsonObject);
        return "success";
    }

    @Transactional
    @Tool("Delete one or more records from the specified erupt model by their primary key IDs.")
    public String deleteEruptData(
            @P(ERUPT_NAME_PARAM_HINT) String eruptName,
            @P("List of primary key values identifying the records to delete. Use findEruptDataByPk or eruptDataQuery to confirm IDs before deletion.") List<Object> ids) {
        eruptModifyService.deleteEruptData(EruptCoreService.getErupt(eruptName), ids, false);
        return "success";
    }

    @Tool("Generate erupt annotation code. Returns the erupt annotation reference documentation to guide code generation.")
    public String geneEruptCode() throws Exception {
        try (var in = getClass().getClassLoader().getResourceAsStream("erupt-annotation.md")) {
            return new String(in.readAllBytes());
        }
    }

}
