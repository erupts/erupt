package xyz.erupt.ai.controller;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.internal.JsonSchemaElementJsonUtils;
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
import xyz.erupt.ai.vo.mcp.*;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.EruptInformation;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptOpenApi;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptTokenService;

import java.io.IOException;
import java.lang.reflect.Method;
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

    @Resource
    private EruptTokenService eruptTokenService;

    // a call's session outlives it only if the call never returns
    private static final int SESSION_MINUTES = 5;

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
        EruptUser user = null;
        if (null != grant.uid()) {
            // looked up across tenants, then the call runs in the user's own tenant: their data, not the platform's
            user = eruptDao.lambdaQuery(EruptUser.class).allTenants().eq(EruptUser::getId, grant.uid()).one();
            if (null == user) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            MetaContext.getUser().setUid(user.getId());
            MetaContext.getUser().setName(user.getName());
            MetaContext.getUser().setAccount(user.getAccount());
            MetaContext.getUser().setTenantId(UpmsTenant.isPlatform(user.getTenantId()) ? null : user.getTenantId());
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
                // tools that check permissions read the user's session, as in AI chat: one lives for this call only
                String session = null == user ? null : this.openSession(user);
                try {
                    content.setText(McpContext.call(grant, () -> this.mcpCall(grant, name,
                            null == request.getParams() ? null : request.getParams().getArguments())));
                } catch (Exception e) {
                    log.error("MCP call error [" + name + "]", e);
                    content.setText(e.getMessage());
                    mcpCallResult.setError(true);
                } finally {
                    if (null != session) eruptTokenService.logoutToken(user.getAccount(), session);
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

    // a short session of the user, registered as the current token: menus and powers as at login
    private String openSession(EruptUser user) {
        String session = "MCP" + Erupts.generateCode(24).toUpperCase();
        eruptTokenService.loginToken(user, session, SESSION_MINUTES);
        MetaContext.registerToken(session);
        return session;
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

    // erupt's Gson writes nulls, and a schema with "items": null is rejected whole: nulls never go out
    @SuppressWarnings("unchecked")
    private static Map<String, Object> withoutNulls(Map<String, Object> map) {
        Map<String, Object> clean = new LinkedHashMap<>();
        map.forEach((k, v) -> {
            if (v instanceof Map<?, ?> m) clean.put(k, withoutNulls((Map<String, Object>) m));
            else if (v instanceof List<?> l) clean.put(k, l.stream().filter(Objects::nonNull)
                    .map(it -> it instanceof Map<?, ?> m ? withoutNulls((Map<String, Object>) m) : it).toList());
            else if (null != v) clean.put(k, v);
        });
        return clean;
    }

    private static Method method(String name) {
        Method method = AiToolboxManager.getAiMethodMap().get(name);
        return null != method ? method : AiToolboxManager.getGrantedMethodMap().get(name);
    }

    // the schema langchain4j builds for chat models, so a tool reads the same to an MCP client as in AI chat
    private List<McpTool> mcpTools(Set<String> allowedTools) {
        List<McpTool> mcpTools = new ArrayList<>();
        for (String name : allowedTools) {
            Method method = method(name);
            if (null == method || null == method.getAnnotation(Tool.class)) continue;
            ToolSpecification spec = ToolSpecifications.toolSpecificationFrom(method);
            McpTool mcpTool = new McpTool();
            mcpTool.setName(name);
            mcpTool.setDescription(StringUtils.defaultString(spec.description()));
            Map<String, Object> schema = null == spec.parameters() ? new LinkedHashMap<>(Map.of("type", "object"))
                    : new LinkedHashMap<>(JsonSchemaElementJsonUtils.toMap(spec.parameters()));
            schema.putIfAbsent("properties", new LinkedHashMap<>());
            mcpTool.setInputSchema(withoutNulls(schema));
            mcpTools.add(mcpTool);
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
