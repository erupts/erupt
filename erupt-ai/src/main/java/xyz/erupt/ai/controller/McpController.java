package xyz.erupt.ai.controller;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.config.AiMCPProp;
import xyz.erupt.ai.tool.AiToolboxManager;
import xyz.erupt.ai.util.McpUtil;
import xyz.erupt.ai.vo.mcp.*;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.EruptInformation;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptOpenApi;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@ConditionalOnProperty(name = "erupt.ai.mcp.server-enabled", havingValue = "true")
@RestController
@RequestMapping("/mcp")
@Slf4j
public class McpController {

    @Resource
    private AiMCPProp mcpProp;

    @Resource
    private EruptDao eruptDao;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PostMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sse() {
        SseEmitter emitter = new SseEmitter(0L);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                emitter.send(SseEmitter.event().name("initialize").data(this.mcpInfo()));
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "mcp-sse-ping"));
            ScheduledFuture<?> pingTask = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    emitter.send(SseEmitter.event().name("ping"));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }, 15, 15, TimeUnit.SECONDS);
            emitter.onCompletion(() -> cleanup(scheduler, pingTask));
            emitter.onTimeout(() -> cleanup(scheduler, pingTask));
            emitter.onError(e -> cleanup(scheduler, pingTask));
        });
        return emitter;
    }

    private void cleanup(ScheduledExecutorService scheduler, ScheduledFuture<?> task) {
        task.cancel(false);
        scheduler.shutdownNow();
    }

    @PostMapping
    public ResponseEntity<?> call(HttpServletRequest servletRequest, @RequestBody McpRequest request) {
        String auth = servletRequest.getHeader("Authorization");
        if (null == auth) {
            log.error("MCP authorization not found");
            return ResponseEntity.status(401).build();
        }
        String bearer = "Bearer ";
        String token = auth.startsWith(bearer) ? auth.substring(bearer.length()) : auth;
        EruptOpenApi eruptOpenApi = eruptDao.lambdaQuery(EruptOpenApi.class).eq(EruptOpenApi::getSecret, token).one();
        if (null == eruptOpenApi) {
            log.error("MCP authorization token error: {}", token);
            return ResponseEntity.status(401).build();
        } else {
            MetaContext.getUser().setUid(eruptOpenApi.getEruptUser().getId());
            MetaContext.getUser().setName(eruptOpenApi.getEruptUser().getName());
            MetaContext.getUser().setAccount(eruptOpenApi.getEruptUser().getAccount());
        }
        McpResult result = new McpResult();
        switch (request.getMethod()) {
            case "initialize" -> result.setResult(this.mcpInfo());
            case "notifications/initialized" -> {
            }
            case "tools/list" -> result.setResult(Map.of("tools", this.mcpTools()));
            case "tools/call" -> {
                McpCallResult mcpCallResult = new McpCallResult();
                McpCallResult.Content content = new McpCallResult.Content();
                try {
                    content.setText(this.mcpCall(request.getParams().getName(), request.getParams().getArguments()));
                    mcpCallResult.setContent(List.of(content));
                    result.setResult(mcpCallResult);
                } catch (Exception e) {
                    log.error("MCP call error [" + request.getParams().getName() + "]", e);
                    content.setText(e.getMessage());
                    mcpCallResult.setError(true);
                    mcpCallResult.setContent(List.of(content));
                    result.setResult(mcpCallResult);
                }
            }
            default -> {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", Map.of("code", -32601, "message", "Method not found " + request.getMethod())));
            }
        }
        result.setId(request.getId());
        return ResponseEntity.ok(result);
    }

    private McpInfo mcpInfo() {
        McpInfo mcpInfo = new McpInfo();
        mcpInfo.setProtocolVersion("2024-11-05");
        mcpInfo.setCapabilities(Map.of("tools", new HashMap<>()));
        McpInfo.ServerInfo serverInfo = new McpInfo.ServerInfo();
        serverInfo.setName(mcpProp.getName());
        serverInfo.setVersion(EruptInformation.getEruptVersion());
        mcpInfo.setServerInfo(serverInfo);
        return mcpInfo;
    }

    private List<McpTool> mcpTools() {
        List<McpTool> mcpTools = new ArrayList<>();
        for (Map.Entry<String, Method> entry : AiToolboxManager.getAiMethodMap().entrySet()) {
            Method method = entry.getValue();
            Tool tool = method.getAnnotation(Tool.class);
            if (null != tool) {
                McpTool mcpTool = new McpTool();
                mcpTools.add(mcpTool);
                mcpTool.setName(entry.getKey());
                mcpTool.setDescription(tool.value().length > 0 ? tool.value()[0] : "");
                {
                    McpTool.InputSchema inputSchema = new McpTool.InputSchema();
                    mcpTool.setInputSchema(inputSchema);
                    List<String> required = new ArrayList<>();
                    for (Parameter parameter : method.getParameters()) {
                        P p = parameter.getAnnotation(P.class);
                        String description = "";
                        if (null != p) {
                            description = p.value();
                            required.add(parameter.getName());
                        }
                        McpTool.SchemaProperties schema = new McpTool.SchemaProperties();
                        schema.setType(McpUtil.toMcp(parameter.getType()));
                        schema.setDescription(description);
                        mcpTool.getInputSchema().getProperties().put(parameter.getName(), schema);
                    }
                    mcpTool.getInputSchema().setRequired(required);
                }
            }
        }
        return mcpTools;
    }

    @SneakyThrows
    private String mcpCall(String code, Map<String, Object> params) {
        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .name(code)
                .arguments(GsonFactory.getGson().toJson(params))
                .build();
        return (String) AiToolboxManager.invoke(request);
    }

}
