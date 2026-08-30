package xyz.erupt.cloud.server.interceptor;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.cloud.server.config.EruptCloudServerProp;
import xyz.erupt.cloud.server.node.MetaNode;
import xyz.erupt.cloud.server.node.NodeContext;
import xyz.erupt.cloud.server.node.NodeManager;
import xyz.erupt.cloud.server.util.CloudServerUtil;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.constant.EruptReqHeader;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.security.interceptor.EruptSecurityInterceptor;
import xyz.erupt.security.service.OperationService;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author YuePeng
 * date 2018-12-20.
 */
@Configuration
@Component
@Slf4j
@Order(Integer.MAX_VALUE - 1)
public class EruptCloudServerInterceptor implements WebMvcConfigurer, AsyncHandlerInterceptor {

    @Resource
    private EruptContextService eruptContextService;

    @Resource
    private NodeManager nodeManager;

    @Resource
    private EruptSessionService eruptSessionService;

    @Resource
    private EruptSecurityInterceptor eruptSecurityInterceptor;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private OperationService operationService;

    @Resource
    private EruptCloudServerProp eruptCloudServerProp;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns(EruptRestPath.ERUPT_API + "/**");
    }

    private static final String[] TRANSFER_HEADERS = {
            HttpHeaders.CONTENT_DISPOSITION
    };

    // tpl pages carried by a node are routed by an explicit "_node" query param (there is no erupt
    // to key on); the path itself stays node-local and is forwarded verbatim.
    private static final String URL_NODE_PARAM = "_node";

    private static final String TPL_PATH = EruptRestPath.ERUPT_API + "/tpl/";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull Object handler) throws Exception {
        EruptRouter eruptRouter = null;
        if (handler instanceof HandlerMethod) {
            eruptRouter = ((HandlerMethod) handler).getMethodAnnotation(EruptRouter.class);
        }
        if (null == eruptRouter) return true;
        // tpl page carried by a node: routed by an explicit "_node" param, path stays node-local.
        String tplNode = request.getParameter(URL_NODE_PARAM);
        if (StringUtils.isNotBlank(tplNode) && request.getRequestURI().contains(TPL_PATH)) {
            MetaNode metaNode = nodeManager.getNode(tplNode);
            if (null == metaNode) {
                throw new EruptWebApiRuntimeException("'" + tplNode + "' node not ready");
            }
            String token = eruptContextService.getCurrentToken();
            if (null == token || null == eruptSessionService.get(SessionKey.TOKEN_OLINE + token)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value());
                return false;
            }
            // menu-permission map keys strip the query string, so authorize by the tpl path
            String uri = request.getRequestURI();
            String authStr = uri.substring(uri.indexOf(TPL_PATH) + TPL_PATH.length());
            if (null == eruptUserService.getEruptMenuByValue(authStr)) {
                response.sendError(HttpStatus.FORBIDDEN.value());
                return false;
            }
            NodeContext.set(metaNode);
            MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfo();
            MetaContext.register(new MetaUser(metaUserinfo.getId(), metaUserinfo.getAccount(), metaUserinfo.getUsername()));
            MetaContext.registerToken(token);
            return proxyAndRespond(request, response, handler, metaNode, tplNode, null, null);
        }
        if (EruptRouter.VerifyType.ERUPT == eruptRouter.verifyType()) {
            String erupt = null;
            String authErupt = null;
            if (eruptRouter.verifyMethod() == EruptRouter.VerifyMethod.HEADER) {
                erupt = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_KEY);
                authErupt = request.getHeader(EruptReqHeaderConst.ERUPT_PARENT_HEADER_KEY);
                if (StringUtils.isBlank(authErupt)) {
                    authErupt = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_KEY);
                }
            } else if (eruptRouter.verifyMethod() == EruptRouter.VerifyMethod.PARAM) {
                erupt = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_KEY);
                authErupt = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARENT_PARAM_KEY);
                if (StringUtils.isBlank(authErupt)) {
                    authErupt = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_KEY);
                }
            }
            if (erupt == null) {
                return true;
            }
            if (!erupt.contains(EruptConst.DOT)) {
                return true;
            }
            String token = eruptContextService.getCurrentToken();
            if (null == token || null == eruptSessionService.get(SessionKey.TOKEN_OLINE + token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.sendError(HttpStatus.UNAUTHORIZED.value());
                return false;
            }
            if (null == eruptUserService.getEruptMenuByValue(authErupt)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.sendError(HttpStatus.FORBIDDEN.value());
                return false;
            }
            int point = erupt.lastIndexOf(EruptConst.DOT);
            String nodeName = erupt.substring(0, point);
            String eruptName = erupt.substring(point + 1);
            MetaNode metaNode = nodeManager.getNode(nodeName);
            NodeContext.set(metaNode);
            if (null == metaNode) {
                throw new EruptWebApiRuntimeException("'" + nodeName + "' node not ready");
            }
            MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfo();
            MetaContext.register(new MetaUser(metaUserinfo.getId(), metaUserinfo.getAccount(), metaUserinfo.getUsername()));
            MetaContext.register(new MetaErupt(erupt));
            MetaContext.registerToken(token);
            return proxyAndRespond(request, response, handler, metaNode, nodeName, erupt, eruptName);
        } else {
            return true;
        }
    }

    // Forward the current request to the target node and stream its response back. eruptName == null
    // means the path is forwarded verbatim (tpl pages); otherwise the node-name prefix in the path is
    // rewritten to the node-local erupt name (and @Erupt build responses get their prefixes restored).
    private boolean proxyAndRespond(HttpServletRequest request, HttpServletResponse response, Object handler,
                                    MetaNode metaNode, String nodeName, String erupt, String eruptName) throws Exception {
        String path = null == eruptName ? request.getRequestURI() : request.getRequestURI().replace(erupt, eruptName);
        try (HttpResponse httpResponse = this.httpProxy(request, metaNode, path.substring(path.indexOf(EruptRestPath.ERUPT_API)), eruptName)) {
            Optional.ofNullable(httpResponse.header("Content-Type")).ifPresent(response::setContentType);
            for (String transferHeader : TRANSFER_HEADERS) {
                Optional.ofNullable(httpResponse.header(transferHeader)).ifPresent(it -> response.addHeader(transferHeader, it));
            }
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            // Error: body is small, buffer it for logging and passthrough
            if (httpResponse.getStatus() != HttpStatus.OK.value()) {
                String body = httpResponse.body();
                log.error("{}: {} -> {}", metaNode.getNodeName(), path, body);
                operationService.record(handler, new Exception(body));
                response.setStatus(httpResponse.getStatus());
                response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
                return false;
            }
            operationService.record(handler, null);
            if (null != erupt && (EruptRestPath.ERUPT_BUILD + "/" + erupt).equals(request.getServletPath())) {
                // Build: must buffer to rewrite node-name prefixes into the model
                EruptBuildModel eruptBuildModel = GsonFactory.getGson().fromJson(httpResponse.body(), EruptBuildModel.class);
                this.eruptBuildProcess(eruptBuildModel, nodeName);
                response.getOutputStream().write(GsonFactory.getGson().toJson(eruptBuildModel).getBytes(StandardCharsets.UTF_8));
            } else {
                // Everything else (data / excel / file / large responses): stream through without full buffering
                StreamUtils.copy(httpResponse.bodyStream(), response.getOutputStream());
                response.flushBuffer();
            }
            NodeContext.remove();
            return false;
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull Object handler, Exception ex) throws Exception {
        eruptSecurityInterceptor.afterConcurrentHandlingStarted(request, response, handler);
        NodeContext.remove();
    }

    public HttpResponse httpProxy(HttpServletRequest request, MetaNode metaNode, String path, String eruptName) throws Exception {
        Map<String, String> headers = new CaseInsensitiveMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        headers.remove(HttpHeaders.HOST);
        // Strip the browser Origin — this is a trusted server-to-server forward, and the node rejects
        // Origin-bearing (i.e. browser-direct) calls.
        headers.remove(HttpHeaders.ORIGIN);
        headers.put(CloudCommonConst.HEADER_ACCESS_TOKEN, metaNode.getAccessToken());
        headers.put(EruptMutualConst.TOKEN, eruptContextService.getCurrentToken());
        // tpl page proxy carries no erupt; the node interceptor only checks this header when present
        if (null != eruptName) {
            headers.put(EruptMutualConst.ERUPT, eruptName);
        }
        headers.put(EruptMutualConst.USER, Base64Encoder.encode(GsonFactory.getGson().toJson(MetaContext.getUser())));
        //Process drill header
        if (headers.containsKey(EruptReqHeader.DRILL_SOURCE_ERUPT)) {
            headers.computeIfPresent(EruptReqHeader.DRILL_SOURCE_ERUPT, (k, dse) -> dse.substring(dse.lastIndexOf(".") + 1));
        }
        String query = null == request.getQueryString() ? "" : "?" + request.getQueryString();
        Method method = Method.valueOf(request.getMethod());
        // Buffer the body once so it can be replayed to a failover instance without re-reading the
        // (one-shot) servlet input stream.
        boolean multipart = null != request.getContentType() && request.getContentType().contains("multipart/form-data");
        List<Part> parts = multipart ? new ArrayList<>(request.getParts()) : null;
        Map<Part, byte[]> partBodies = new java.util.IdentityHashMap<>();
        byte[] body = null;
        if (multipart) {
            for (Part part : parts) {
                partBodies.put(part, StreamUtils.copyToByteArray(part.getInputStream()));
            }
        } else {
            body = StreamUtils.copyToByteArray(request.getInputStream());
        }
        List<String> locations = nodeManager.pickLocations(metaNode);
        Exception lastError = null;
        for (int i = 0; i < locations.size(); i++) {
            String location = locations.get(i);
            try {
                HttpRequest httpRequest = HttpUtil.createRequest(method, location + path + query);
                if (multipart) {
                    for (Part part : parts) {
                        httpRequest.form(part.getName(), partBodies.get(part), part.getSubmittedFileName());
                    }
                } else {
                    httpRequest.body(body);
                }
                httpRequest.timeout(eruptCloudServerProp.getNodeRequestTimeout());
                // async execution keeps the body lazy so it can be streamed instead of fully buffered
                return httpRequest.addHeaders(headers).executeAsync();
            } catch (Exception e) {
                lastError = e;
                // Only fail over when the connection never reached the node — otherwise a retry could
                // duplicate a write that the node already processed.
                if (i + 1 < locations.size() && CloudServerUtil.isConnectFailure(e)) {
                    log.warn("node {} instance {} unreachable, failing over: {}", metaNode.getNodeName(), location, e.getMessage());
                    nodeManager.evictInstance(metaNode.getNodeName(), location);
                    continue;
                }
                throw new EruptWebApiRuntimeException(location + " -> " + e.getMessage());
            }
        }
        throw new EruptWebApiRuntimeException(metaNode.getNodeName() + " -> " + (null == lastError ? "no available instance" : lastError.getMessage()));
    }

    private void eruptBuildProcess(EruptBuildModel eruptBuildModel, String nodeName) {
        String prefix = nodeName + EruptConst.DOT;
        eruptBuildModel.getEruptModel().setEruptName(prefix + eruptBuildModel.getEruptModel().getEruptName());
        //Modify the Drill value
        JsonArray drills = eruptBuildModel.getEruptModel().getEruptJson().getAsJsonArray("drills");
        if (!drills.isEmpty()) {
            for (JsonElement drill : drills) {
                JsonObject link = drill.getAsJsonObject().get("link").getAsJsonObject();
                link.addProperty("linkErupt", prefix + link.get("linkErupt").getAsString());
            }
        }
        Optional.ofNullable(eruptBuildModel.getOperationErupts()).ifPresent(it -> {
            for (EruptModel value : it.values()) {
                value.setEruptName(prefix + value.getEruptName());
            }
        });
        Optional.ofNullable(eruptBuildModel.getTabErupts()).ifPresent(it -> {
            for (EruptBuildModel value : it.values()) {
                value.getEruptModel().setEruptName(prefix + value.getEruptModel().getEruptName());
            }
        });
        Optional.ofNullable(eruptBuildModel.getCombineErupts()).ifPresent(it -> {
            for (EruptModel value : it.values()) {
                value.setEruptName(prefix + value.getEruptName());
            }
        });
    }

}
