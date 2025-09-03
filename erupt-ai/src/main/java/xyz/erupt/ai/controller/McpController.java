package xyz.erupt.ai.controller;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.annotation.AiParam;
import xyz.erupt.ai.call.AiFunctionCall;
import xyz.erupt.ai.call.AiFunctionManager;
import xyz.erupt.ai.config.AiMCPProp;
import xyz.erupt.ai.util.McpUtil;
import xyz.erupt.ai.vo.mcp.McpInfo;
import xyz.erupt.ai.vo.mcp.McpRequest;
import xyz.erupt.ai.vo.mcp.McpTool;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/mcp")
public class McpController {

    @Resource
    private AiMCPProp mcpProp;

    public static final String INITIALIZE = "initialize";

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sse() {
        SseEmitter emitter = new SseEmitter(0L);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                emitter.send(SseEmitter.event().name(INITIALIZE).data(this.mcpInfo()));
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
        Object result;
        switch (request.getMethod()) {
            case INITIALIZE -> result = this.mcpInfo();
            case "tools/list" -> {
                List<McpTool> mcpTools = new ArrayList<>();
                result = mcpTools;
                for (Map.Entry<String, AiFunctionCall> entry : AiFunctionManager.getAiFunctions().entrySet()) {
                    McpTool mcpTool = new McpTool();
                    mcpTools.add(mcpTool);
                    mcpTool.setName(entry.getValue().code());
                    mcpTool.setDescription(entry.getValue().description());
                    McpTool.InputSchema inputSchema = new McpTool.InputSchema();
                    mcpTool.setInputSchema(inputSchema);
                    {
                        List<String> required = new ArrayList<>();
                        for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                            AiParam aiParam = field.getDeclaredAnnotation(AiParam.class);
                            if (null != aiParam) {
                                if (aiParam.required()) {
                                    required.add(field.getName());
                                }
                                mcpTool.getInputSchema().setType("object");
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
            case "tools/call" ->
                    result = this.mcpCall(request.getParams().getName(), request.getParams().getArguments());
            default -> {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", Map.of("code", -32601, "message", "Method not found " + request.getMethod())));
            }
        }
        return ResponseEntity.ok(Map.of("jsonrpc", "2.0", "id", request.getId(), "result", result));
    }

    private McpInfo mcpInfo() {
        McpInfo mcpInfo = new McpInfo();
        mcpInfo.setProtocolVersion("1");
        mcpInfo.setCapabilities(Map.of("tools", Map.of()));
        McpInfo.ServerInfo serverInfo = new McpInfo.ServerInfo();
        serverInfo.setName("erupt-mcp");
        serverInfo.setVersion("1.0");
        mcpInfo.setServerInfo(serverInfo);
        return mcpInfo;
    }

    private List<McpTool> mcpTools() {
        return null;
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
