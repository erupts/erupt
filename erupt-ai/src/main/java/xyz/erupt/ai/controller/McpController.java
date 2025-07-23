package xyz.erupt.ai.controller;

import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.ai.annotation.AiParam;
import xyz.erupt.ai.call.AiFunctionCall;
import xyz.erupt.ai.call.AiFunctionManager;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.vo.McpVo;
import xyz.erupt.ai.vo.OpenAiVo;
import xyz.erupt.core.util.EruptInformation;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

@RestController
public class McpController {

    @Resource
    private AiProp aiProp;

    public static final String MCP_JSON = "/mcp.json";

    public static final String OPEN_AI_JSON = "/openapi.json";

    public static final String MCP_CALL_API = "/mcp/call";

    @GetMapping(MCP_JSON)
    public McpVo mcp() {
        McpVo mcpVo = new McpVo();
        mcpVo.setName_for_human(aiProp.getNameForHuman());
        mcpVo.setDescription_for_human(aiProp.getDescriptionForHuman());
        mcpVo.setDescription_for_model(aiProp.getDescriptionForModel());
        mcpVo.setContact_email(aiProp.getContactEmail());
        mcpVo.setLegal_info_url(aiProp.getLegalInfoUrl());
        mcpVo.getApi().setUrl(aiProp.getApiDomain() + OPEN_AI_JSON);
        return mcpVo;
    }

    @GetMapping(OPEN_AI_JSON)
    public OpenAiVo eruptAi() {
        OpenAiVo openAiVo = new OpenAiVo();
        OpenAiVo.Info info = new OpenAiVo.Info();
        info.setTitle(aiProp.getNameForHuman());
        info.setVersion(EruptInformation.getEruptVersion());
        info.setDescription(aiProp.getDescriptionForModel());
        openAiVo.setInfo(info);
        openAiVo.setServers(new OpenAiVo.Server[]{new OpenAiVo.Server(aiProp.getApiDomain())});
        for (Map.Entry<String, AiFunctionCall> entry : AiFunctionManager.getAiFunctions().entrySet()) {
            OpenAiVo.Path path = new OpenAiVo.Path();
            openAiVo.getPaths().put(MCP_CALL_API + "/" + entry.getKey(), path);
            path.setSummary(entry.getValue().description());
            path.setOperationId(entry.getKey());
            OpenAiVo.RequestBody requestBody = new OpenAiVo.RequestBody();
            path.setRequestBody(requestBody);
            requestBody.setRequired(true);
            OpenAiVo.ApplicationType applicationType = new OpenAiVo.ApplicationType();
            requestBody.getContent().put("application/json", applicationType);
            applicationType.setSchema(new OpenAiVo.Schema());
        }
        return openAiVo;
    }

    @PostMapping(MCP_CALL_API + "/{code}")
    @SneakyThrows
    public String mcpCall(@PathVariable String code, @RequestBody(required = false) Map<String, Object> params) {
        AiFunctionCall aiFunctionCall = AiFunctionManager.getAiFunctions().get(code);
        if (null != params) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                Field field = aiFunctionCall.getClass().getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(aiFunctionCall, entry.getValue());
                field.setAccessible(false);
            }
        }
        return aiFunctionCall.call(null);
    }

}
