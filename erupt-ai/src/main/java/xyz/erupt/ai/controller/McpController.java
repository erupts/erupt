package xyz.erupt.ai.controller;

import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.ai.annotation.AiParam;
import xyz.erupt.ai.call.AiFunctionCall;
import xyz.erupt.ai.call.AiFunctionManager;
import xyz.erupt.ai.config.AiMCPProp;
import xyz.erupt.ai.util.McpUtil;
import xyz.erupt.ai.vo.OpenAiVo;
import xyz.erupt.core.util.EruptInformation;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class McpController {

    @Resource
    private AiMCPProp mcpProp;

    private static final String MCP_CALL_API = "/mcp/call";

    @GetMapping("/openapi.json")
    public OpenAiVo eruptAi() {
        OpenAiVo openAiVo = new OpenAiVo();
        OpenAiVo.Info info = new OpenAiVo.Info();
        info.setTitle(mcpProp.getName());
        info.setVersion(EruptInformation.getEruptVersion());
        info.setDescription(mcpProp.getDescription());
        openAiVo.setInfo(info);
        openAiVo.setServers(new OpenAiVo.Server[]{new OpenAiVo.Server(mcpProp.getApiDomain())});
        for (Map.Entry<String, AiFunctionCall> entry : AiFunctionManager.getAiFunctions().entrySet()) {
            OpenAiVo.Path path = new OpenAiVo.Path();
            openAiVo.getPaths().put(MCP_CALL_API + "/" + entry.getKey(), path);
            OpenAiVo.PathPost post = new OpenAiVo.PathPost();
            path.setPost(post);
            post.setSummary(entry.getValue().description());
            post.setOperationId(entry.getKey());
            {
                List<String> required = new ArrayList<>();
                Map<String, OpenAiVo.SchemaProperties> properties = new HashMap<>();
                for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                    AiParam aiParam = field.getDeclaredAnnotation(AiParam.class);
                    if (null != aiParam) {
                        if (aiParam.required()) {
                            required.add(field.getName());
                        }
                        OpenAiVo.SchemaProperties schemaProperties = new OpenAiVo.SchemaProperties();
                        schemaProperties.setType(McpUtil.toMcp(field.getType()));
                        schemaProperties.setDescription(aiParam.description());
                        properties.put(field.getName(), schemaProperties);
                    }
                }

                OpenAiVo.RequestBody requestBody = new OpenAiVo.RequestBody();
                post.setRequestBody(requestBody);
                requestBody.setRequired(!properties.isEmpty());
                OpenAiVo.ApplicationType applicationType = new OpenAiVo.ApplicationType();
                requestBody.getContent().put("application/json", applicationType);
                OpenAiVo.Schema schema = new OpenAiVo.Schema();
                applicationType.setSchema(schema);
                schema.setType("object");
                schema.setRequired(required);
                schema.setProperties(properties);
            }
            {
                OpenAiVo.Response response = new OpenAiVo.Response();
                post.getResponses().put(200, response);
                response.setDescription("Success");
                OpenAiVo.ApplicationType applicationType = new OpenAiVo.ApplicationType();
                OpenAiVo.Schema schema = new OpenAiVo.Schema();
                schema.setType(McpUtil.toMcp(String.class));
                schema.setRequired(new ArrayList<>());
                applicationType.setSchema(schema);
                response.getContent().put("text/plain", applicationType);
            }
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
