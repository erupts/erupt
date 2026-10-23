package xyz.erupt.ai.controller;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.config.AiMCPProp;
import xyz.erupt.ai.core.AiToolboxManager;
import xyz.erupt.ai.mcp.McpAuthenticator;
import xyz.erupt.ai.mcp.McpContext;
import xyz.erupt.ai.mcp.McpGrant;
import xyz.erupt.ai.service.LLMRoleService;
import xyz.erupt.ai.util.McpUtil;
import xyz.erupt.ai.vo.mcp.*;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.EruptInformation;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptOpenApi;
import xyz.erupt.upms.model.EruptUser;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * erupt's MCP server: the @AiToolbox tools over JSON-RPC. A user reaches it with an open-api secret when
 * erupt.ai.mcp.server-enabled is on and sees the tools of their AI roles; a module's own tokens
 * (an {@link McpAuthenticator}, e.g. a coding agent run) always work and see exactly what their grant names.
 */
@RestController
@RequestMapping("/mcp")
@Slf4j
public class McpController {

    @Resource
    private AiMCPProp mcpProp;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private LLMRoleService llmRoleService;

    @Resource
    private ObjectProvider<McpAuthenticator> authenticators;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PostMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> sse() {
        if (!mcpProp.isServerEnabled()) return ResponseEntity.notFound().build();
        SseEmitter emitter = new SseEmitter(0L);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                emitter.send(SseEmitter.event().name("initialize").data(this.mcpInfo(null)));
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
        return ResponseEntity.ok(emitter);
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String bearer = "Bearer ";
        String token = (auth.startsWith(bearer) ? auth.substring(bearer.length()) : auth).trim();
        McpGrant grant = this.grant(token);
        if (null == grant) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (null != grant.uid()) {
            EruptUser user = eruptDao.find(EruptUser.class, grant.uid());
            if (null == user) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            MetaContext.getUser().setUid(user.getId());
            MetaContext.getUser().setName(user.getName());
            MetaContext.getUser().setAccount(user.getAccount());
        }
        // a notification (no id) gets no answer
        if (null == request.getId()) return ResponseEntity.accepted().build();
        McpResult result = new McpResult();
        result.setId(request.getId());
        switch (StringUtils.defaultString(request.getMethod())) {
            case "initialize" -> result.setResult(this.mcpInfo(request.getParams()));
            case "ping" -> result.setResult(Map.of());
            case "tools/list" -> result.setResult(Map.of("tools", this.mcpTools(grant.tools())));
            case "resources/list" -> result.setResult(Map.of("resources", List.of()));
            case "prompts/list" -> result.setResult(Map.of("prompts", List.of()));
            case "tools/call" -> {
                McpCallResult mcpCallResult = new McpCallResult();
                McpCallResult.Content content = new McpCallResult.Content();
                String name = null == request.getParams() ? null : request.getParams().getName();
                try {
                    content.setText(McpContext.call(grant, () -> this.mcpCall(grant, name,
                            null == request.getParams() ? null : request.getParams().getArguments())));
                } catch (Exception e) {
                    log.error("MCP call error [" + name + "]", e);
                    content.setText(e.getMessage());
                    mcpCallResult.setError(true);
                }
                mcpCallResult.setContent(List.of(content));
                result.setResult(mcpCallResult);
            }
            default -> {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", Map.of("code", -32601, "message", "Method not found " + request.getMethod())));
            }
        }
        return ResponseEntity.ok(result);
    }

    // a module's own token first, then a user's open-api secret when the user server is on
    private McpGrant grant(String token) {
        if (StringUtils.isBlank(token)) return null;
        for (McpAuthenticator authenticator : authenticators) {
            McpGrant grant = authenticator.authenticate(token);
            if (null != grant) return grant;
        }
        if (!mcpProp.isServerEnabled()) return null;
        // A locked record keeps its secret but must not authenticate, same as the open-api login
        EruptOpenApi eruptOpenApi = eruptDao.lambdaQuery(EruptOpenApi.class)
                .eq(EruptOpenApi::getSecret, token).eq(EruptOpenApi::getStatus, true).one();
        if (null == eruptOpenApi) {
            log.error("MCP authorization token rejected");
            return null;
        }
        Long uid = eruptOpenApi.getEruptUser().getId();
        return new McpGrant(uid, llmRoleService.getAllowedToolsByUid(uid), null);
    }

    private McpInfo mcpInfo(McpRequest.RequestParams params) {
        McpInfo mcpInfo = new McpInfo();
        // answer in the client's protocol version: the subset served here reads the same in all of them
        if (null != params && StringUtils.isNotBlank(params.getProtocolVersion())) mcpInfo.setProtocolVersion(params.getProtocolVersion());
        mcpInfo.setCapabilities(Map.of("tools", new HashMap<>()));
        McpInfo.ServerInfo serverInfo = new McpInfo.ServerInfo();
        serverInfo.setName(mcpProp.getName());
        serverInfo.setVersion(EruptInformation.getEruptVersion());
        mcpInfo.setServerInfo(serverInfo);
        return mcpInfo;
    }

    private static Method method(String name) {
        Method method = AiToolboxManager.getAiMethodMap().get(name);
        return null != method ? method : AiToolboxManager.getGrantedMethodMap().get(name);
    }

    private List<McpTool> mcpTools(Set<String> allowedTools) {
        List<McpTool> mcpTools = new ArrayList<>();
        for (String name : allowedTools) {
            Method method = method(name);
            Tool tool = null == method ? null : method.getAnnotation(Tool.class);
            if (null == tool) continue;
            McpTool mcpTool = new McpTool();
            mcpTools.add(mcpTool);
            mcpTool.setName(name);
            mcpTool.setDescription(String.join("\n", tool.value()));
            McpTool.InputSchema inputSchema = new McpTool.InputSchema();
            mcpTool.setInputSchema(inputSchema);
            for (Parameter parameter : method.getParameters()) {
                P p = parameter.getAnnotation(P.class);
                McpTool.SchemaProperties schema = new McpTool.SchemaProperties();
                schema.setType(McpUtil.toMcp(parameter.getType()));
                schema.setDescription(null == p ? "" : p.value());
                inputSchema.getProperties().put(parameter.getName(), schema);
                if (null != p && p.required()) inputSchema.getRequired().add(parameter.getName());
            }
        }
        mcpTools.sort(Comparator.comparing(McpTool::getName));
        return mcpTools;
    }

    private String mcpCall(McpGrant grant, String code, Map<String, Object> params) {
        if (null == code || !grant.tools().contains(code)) throw new SecurityException("Tool not permitted: " + code);
        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .name(code)
                .arguments(GsonFactory.getGson().toJson(null == params ? Map.of() : params))
                .build();
        return String.valueOf(AiToolboxManager.invokeGranted(request));
    }

}
