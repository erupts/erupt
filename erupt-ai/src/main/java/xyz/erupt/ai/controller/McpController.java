package xyz.erupt.ai.controller;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.annotation.AiParam;
import xyz.erupt.ai.call.AiFunctionCall;
import xyz.erupt.ai.call.AiFunctionManager;
import xyz.erupt.ai.config.AiMCPProp;
import xyz.erupt.ai.util.McpUtil;
import xyz.erupt.ai.vo.mcp.*;
import xyz.erupt.core.util.EruptInformation;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@ConditionalOnProperty(name = "erupt.ai.mcp.enabled", havingValue = "true")
@RestController
@RequestMapping("/mcp")
public class McpController {

    @Resource
    private AiMCPProp mcpProp;

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
    public ResponseEntity<?> call(@RequestBody McpRequest request) {
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
        for (Map.Entry<String, AiFunctionCall> entry : AiFunctionManager.getAiFunctions().entrySet()) {
            if (entry.getValue().mcpCall()) {
                McpTool mcpTool = new McpTool();
                mcpTools.add(mcpTool);
                mcpTool.setName(entry.getValue().name());
                mcpTool.setDescription(entry.getValue().description());
                {
                    McpTool.InputSchema inputSchema = new McpTool.InputSchema();
                    mcpTool.setInputSchema(inputSchema);
                    List<String> required = new ArrayList<>();
                    for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                        AiParam aiParam = field.getDeclaredAnnotation(AiParam.class);
                        if (null != aiParam) {
                            if (aiParam.required()) {
                                required.add(field.getName());
                            }
                            McpTool.SchemaProperties schema = new McpTool.SchemaProperties();
                            schema.setType(McpUtil.toMcp(field.getType()));
                            schema.setDescription(aiParam.description());
                            mcpTool.getInputSchema().getProperties().put(field.getName(), schema);
                        }
                    }
                    mcpTool.getInputSchema().setRequired(required);
                }
            }
        }
        return mcpTools;
    }

    @SneakyThrows
    private String mcpCall(String code, Map<String, Object> params) {
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
