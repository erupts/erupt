package xyz.erupt.cloud.server.interceptor;

import java.net.URI;
import xyz.erupt.cloud.common.http.CloudHttp;
import org.springframework.web.client.RestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.core.io.ByteArrayResource;
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
import org.springframework.web.util.UriUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.cloud.server.config.EruptCloudServerProp;
import xyz.erupt.cloud.server.service.CloudNodeRouter;
import xyz.erupt.cloud.server.service.NodePowerService;
import xyz.erupt.cloud.server.service.NodeExcelService;
import xyz.erupt.cloud.server.service.NodeReferenceService;
import xyz.erupt.core.util.RemoteRequestValidator;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.view.R;
import com.google.gson.JsonParser;
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
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.security.interceptor.EruptSecurityInterceptor;
import xyz.erupt.security.service.OperationService;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

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
    private RestClient nodeRestClient;

    @Resource
    private NodePowerService nodePowerService;

    @Resource
    private NodeReferenceService nodeReferenceService;

    @Resource
    private NodeExcelService nodeExcelService;

    @Resource
    private EruptCloudServerProp eruptCloudServerProp;

    @Resource
    private CloudNodeRouter cloudNodeRouter;

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
        // Server-owned API (record comments and the like): the erupt name may point at a node, but the
        // answer lives here — the node carries neither the module nor the users the data refers to.
        if (!eruptRouter.cloudProxy()) return true;
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
            MetaContext.register(new MetaUser(metaUserinfo.getId(), metaUserinfo.getAccount(), metaUserinfo.getUsername(), metaUserinfo.getTenantId()));
            MetaContext.registerToken(token);
            return proxyAndRespond(request, response, handler, metaNode, tplNode, null, null, null);
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
            // A parent erupt only vouches for a child on the same node; the node itself checks the
            // nesting relation, since only it holds the annotation metadata
            if (!erupt.equals(authErupt) && !authErupt.startsWith(nodeName + EruptConst.DOT)) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return false;
            }
            MetaNode metaNode = nodeManager.getNode(nodeName);
            NodeContext.set(metaNode);
            if (null == metaNode) {
                throw new EruptWebApiRuntimeException("'" + nodeName + "' node not ready");
            }
            MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfo();
            MetaContext.register(new MetaUser(metaUserinfo.getId(), metaUserinfo.getAccount(), metaUserinfo.getUsername(), metaUserinfo.getTenantId()));
            MetaContext.register(new MetaErupt(erupt));
            MetaContext.registerToken(token);
            // The user's rights, checked here so a node does not have to (and cannot forget to)
            PowerObject userPower = nodePowerService.userPower(authErupt);
            if (!NodePowerService.allows(userPower, eruptRouter.power())) {
                throw new EruptNoLegalPowerException();
            }
            // Uploads, and options a field does not fetch on the node, are the server's: its own controller
            // answers them from the node's build, nothing is forwarded
            if (serverAnswers(eruptRouter.resolve(), erupt, request)) {
                return true;
            }
            byte[] body = isMultipart(request) ? null : StreamUtils.copyToByteArray(request.getInputStream());
            Set<CloudSignature.Enforced> enforced = EnumSet.of(CloudSignature.Enforced.POWER);
            if (eruptRouter.payload() != EruptRouter.Payload.NONE) {
                validatePayload(eruptRouter.payload(), erupt, body);
                enforced.add(eruptRouter.payload() == EruptRouter.Payload.TABLE_QUERY
                        ? CloudSignature.Enforced.QUERY : CloudSignature.Enforced.RECORD);
            }
            return proxyAndRespond(request, response, handler, metaNode, nodeName, erupt, eruptName,
                    new Forward(body, userPower, enforced, eruptRouter.resolve()));
        } else {
            return true;
        }
    }

    // Forward the current request to the target node and stream its response back. eruptName == null
    // means the path is forwarded verbatim (tpl pages); otherwise the node-name prefix in the path is
    // rewritten to the node-local erupt name (and @Erupt build responses get their prefixes restored).
    private boolean proxyAndRespond(HttpServletRequest request, HttpServletResponse response, Object handler,
                                    MetaNode metaNode, String nodeName, String erupt, String eruptName, Forward forward) throws Exception {
        String path = null == eruptName ? request.getRequestURI() : request.getRequestURI().replace(erupt, eruptName);
        return this.httpProxy(request, metaNode, path.substring(path.indexOf(EruptRestPath.ERUPT_API)), eruptName, forward, (clientRequest, httpResponse) -> {
            Optional.ofNullable(httpResponse.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).ifPresent(response::setContentType);
            for (String transferHeader : TRANSFER_HEADERS) {
                Optional.ofNullable(httpResponse.getHeaders().getFirst(transferHeader)).ifPresent(it -> response.addHeader(transferHeader, it));
            }
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            int status = httpResponse.getStatusCode().value();
            EruptRouter.Resolve resolve = null == forward ? EruptRouter.Resolve.NONE : forward.resolve();
            // A node that leaves the reference tree or Excel to the server answers 404
            if (status == HttpStatus.NOT_FOUND.value() && FALLBACKS.contains(resolve)) {
                operationService.record(handler, null);
                this.fallback(resolve, request, response, nodeName, erupt, forward);
                return false;
            }
            // Error: body is small, buffer it for logging and passthrough
            if (status != HttpStatus.OK.value()) {
                String body = Optional.ofNullable(httpResponse.bodyTo(String.class)).orElse("");
                log.error("{}: {} -> {}", metaNode.getNodeName(), path, body);
                operationService.record(handler, new Exception(body));
                response.setStatus(status);
                response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
                return false;
            }
            operationService.record(handler, null);
            if (null != erupt && (EruptRestPath.ERUPT_BUILD + "/" + erupt).equals(request.getServletPath())) {
                // Build: must buffer to rewrite node-name prefixes into the model
                EruptBuildModel eruptBuildModel = GsonFactory.getGson().fromJson(httpResponse.bodyTo(String.class), EruptBuildModel.class);
                this.eruptBuildProcess(eruptBuildModel, nodeName);
                // buttons follow the user's rights whatever the node answered
                if (null != forward && null != eruptBuildModel.getPower()) {
                    NodePowerService.restrict(eruptBuildModel.getPower(), forward.userPower());
                }
                response.getOutputStream().write(GsonFactory.getGson().toJson(eruptBuildModel).getBytes(StandardCharsets.UTF_8));
            } else if (resolve == EruptRouter.Resolve.PAGE || resolve == EruptRouter.Resolve.RECORD) {
                // Records: buffered so references a node sends as keys get their labels
                String body = nodeReferenceService.resolve(nodeName, remoteBuild(erupt), resolve == EruptRouter.Resolve.PAGE,
                        Optional.ofNullable(httpResponse.bodyTo(String.class)).orElse("null"));
                response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            } else {
                // Everything else (data / excel / file / large responses): stream through without full buffering
                StreamUtils.copy(httpResponse.getBody(), response.getOutputStream());
                response.flushBuffer();
            }
            NodeContext.remove();
            return false;
        });
    }

    private static final Set<EruptRouter.Resolve> FALLBACKS = EnumSet.of(EruptRouter.Resolve.REFERENCE_TREE,
            EruptRouter.Resolve.EXCEL_TEMPLATE, EruptRouter.Resolve.EXCEL_EXPORT, EruptRouter.Resolve.EXCEL_IMPORT,
            EruptRouter.Resolve.OPTIONS);

    private boolean serverAnswers(EruptRouter.Resolve resolve, String erupt, HttpServletRequest request) {
        return switch (resolve) {
            case UPLOAD -> true;
            case OPTIONS -> !fetchesOptions(erupt, lastPathSegment(request));
            default -> false;
        };
    }

    // Options a handler on the node fetches (choiceType / multiChoiceType / tagsType fetchHandler) are the node's
    // to answer; the build names such a handler, an annotation read back from the build cannot hold its class
    private boolean fetchesOptions(String erupt, String field) {
        EruptFieldModel fieldModel = remoteField(erupt, field);
        // an unknown field is left to the node, which owns the model
        if (null == fieldModel) return true;
        JsonObject edit = fieldModel.getEruptFieldJson().getAsJsonObject(LambdaSee.method(EruptField::edit));
        if (null == edit) return false;
        String fetchHandler = LambdaSee.method(ChoiceType::fetchHandler);
        return Stream.of(LambdaSee.method(Edit::choiceType), LambdaSee.method(Edit::multiChoiceType), LambdaSee.method(Edit::tagsType))
                .map(edit::get).filter(it -> null != it && it.isJsonObject())
                .map(it -> it.getAsJsonObject().get(fetchHandler))
                .anyMatch(it -> null != it && it.isJsonArray() && !it.getAsJsonArray().isEmpty());
    }

    private EruptFieldModel remoteField(String erupt, String field) {
        return Optional.ofNullable(remoteBuild(erupt).getEruptModel().getEruptFieldModels()).orElse(List.of()).stream()
                .filter(it -> field.equals(it.getFieldName()) && null != it.getEruptFieldJson()).findFirst().orElse(null);
    }

    private static String lastPathSegment(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return UriUtils.decode(uri.substring(uri.lastIndexOf('/') + 1), StandardCharsets.UTF_8);
    }

    private void fallback(EruptRouter.Resolve resolve, HttpServletRequest request, HttpServletResponse response,
                          String nodeName, String erupt, Forward forward) throws java.io.IOException {
        EruptBuildModel build = remoteBuild(erupt);
        switch (resolve) {
            case REFERENCE_TREE -> writeJson(response, nodeReferenceService.tree(nodeName, build, lastPathSegment(request)));
            // a field naming a fetch handler the node does not serve: the options its build resolved
            case OPTIONS -> writeJson(response, Optional.ofNullable(remoteField(erupt, lastPathSegment(request)))
                    .map(EruptFieldModel::getComponentValue).orElse(List.of()));
            case EXCEL_TEMPLATE -> nodeExcelService.template(erupt, build, request, response);
            case EXCEL_EXPORT -> nodeExcelService.export(erupt, build, forward.body(), request, response);
            case EXCEL_IMPORT -> writeJson(response, nodeExcelService.importData(erupt, build, request));
            default -> throw new IllegalStateException(resolve.name());
        }
    }

    private static void writeJson(HttpServletResponse response, Object value) throws java.io.IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getOutputStream().write(GsonFactory.getGson().toJson(value).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull Object handler, Exception ex) throws Exception {
        eruptSecurityInterceptor.afterConcurrentHandlingStarted(request, response, handler);
        NodeContext.remove();
    }

    // Hop-by-hop and body-framing headers belong to the outgoing connection; the JDK client rejects
    // them, and Accept-Encoding is dropped so the node streams an identity body we can pass through
    private static final String[] DROP_HEADERS = {
            HttpHeaders.HOST, HttpHeaders.ORIGIN, HttpHeaders.CONTENT_LENGTH, HttpHeaders.CONNECTION,
            HttpHeaders.TRANSFER_ENCODING, HttpHeaders.EXPECT, HttpHeaders.UPGRADE, HttpHeaders.ACCEPT_ENCODING
    };

    /**
     * Forward the incoming servlet request to the node and hand the node response to {@code responder}
     * while it is still open, so large bodies can be streamed instead of buffered.
     */
    private boolean httpProxy(HttpServletRequest request, MetaNode metaNode, String path, String eruptName, Forward forward,
                              RestClient.RequestHeadersSpec.ExchangeFunction<Boolean> responder) throws Exception {
        Map<String, String> headers = new CaseInsensitiveMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        // Origin is stripped on purpose: this is a trusted server-to-server forward, and the node
        // rejects Origin-bearing (i.e. browser-direct) calls.
        for (String drop : DROP_HEADERS) headers.remove(drop);
        headers.put(CloudCommonConst.HEADER_ACCESS_TOKEN, metaNode.getAccessToken());
        headers.put(EruptMutualConst.TOKEN, eruptContextService.getCurrentToken());
        // tpl page proxy carries no erupt; the node interceptor only checks this header when present
        if (null != eruptName) {
            headers.put(EruptMutualConst.ERUPT, eruptName);
        }
        headers.put(EruptMutualConst.USER, CloudHttp.base64(GsonFactory.getGson().toJson(MetaContext.getUser())));
        //Process drill header
        if (headers.containsKey(EruptReqHeader.DRILL_SOURCE_ERUPT)) {
            headers.computeIfPresent(EruptReqHeader.DRILL_SOURCE_ERUPT, (k, dse) -> dse.substring(dse.lastIndexOf(".") + 1));
        }
        String query = null == request.getQueryString() ? "" : "?" + request.getQueryString();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        // Buffer the body once so it can be replayed to a failover instance without re-reading the
        // (one-shot) servlet input stream.
        boolean multipart = isMultipart(request);
        MultiValueMap<String, Object> form = null;
        byte[] body = null;
        if (multipart) {
            // The multipart body is re-encoded by Spring with its own boundary
            headers.remove(HttpHeaders.CONTENT_TYPE);
            form = new LinkedMultiValueMap<>();
            for (Part part : request.getParts()) {
                byte[] bytes = StreamUtils.copyToByteArray(part.getInputStream());
                String filename = part.getSubmittedFileName();
                if (null == filename) {
                    form.add(part.getName(), new String(bytes, StandardCharsets.UTF_8));
                } else {
                    form.add(part.getName(), new ByteArrayResource(bytes) {
                        @Override
                        public String getFilename() {
                            return filename;
                        }
                    });
                }
            }
        } else {
            body = null != forward && null != forward.body() ? forward.body() : StreamUtils.copyToByteArray(request.getInputStream());
        }
        // Signed with the access token, so the token need not travel and nothing signed can be altered
        String enforced = null == forward ? null : CloudSignature.Enforced.header(forward.enforced());
        long timestamp = System.currentTimeMillis() / 1000;
        headers.put(CloudSignature.HEADER_TIMESTAMP, Long.toString(timestamp));
        if (null != enforced) headers.put(CloudSignature.HEADER_ENFORCED, enforced);
        headers.put(CloudSignature.HEADER_SIGNATURE, CloudSignature.sign(metaNode.getAccessToken(), method.name(), path + query,
                timestamp, enforced, headers.get(EruptMutualConst.USER), multipart ? null : body));
        if (!eruptCloudServerProp.isPlainAccessToken()) {
            headers.remove(CloudCommonConst.HEADER_ACCESS_TOKEN);
        }
        List<String> locations = nodeManager.pickLocations(metaNode);
        Exception lastError = null;
        for (int i = 0; i < locations.size(); i++) {
            String location = locations.get(i);
            try {
                // The servlet URI and query string are already encoded: pass them through untouched
                RestClient.RequestBodySpec httpRequest = nodeRestClient.method(method).uri(URI.create(location + path + query))
                        .headers(it -> headers.forEach(it::set));
                if (null != form) {
                    httpRequest.contentType(MediaType.MULTIPART_FORM_DATA).body(form);
                } else if (body.length > 0) {
                    httpRequest.body(body);
                }
                return httpRequest.exchange(responder);
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

    private static boolean isMultipart(HttpServletRequest request) {
        return null != request.getContentType() && request.getContentType().contains(MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    private EruptBuildModel remoteBuild(String erupt) {
        return cloudNodeRouter.resolveBuild(erupt);
    }

    private void validatePayload(EruptRouter.Payload payload, String erupt, byte[] body) {
        JsonElement json;
        try {
            json = null == body || body.length == 0 ? new JsonObject() : JsonParser.parseString(new String(body, StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            throw new EruptApiErrorTip(R.error(I18nTranslate.$translate("erupt.incorrect_format")));
        }
        if (!json.isJsonObject()) {
            throw new EruptApiErrorTip(R.error(I18nTranslate.$translate("erupt.incorrect_format")));
        }
        EruptBuildModel build = remoteBuild(erupt);
        if (payload == EruptRouter.Payload.TABLE_QUERY) {
            String problem = RemoteRequestValidator.validateQuery(build, json.getAsJsonObject());
            if (null != problem) throw new EruptApiErrorTip(R.error(problem));
        } else {
            R<?> problem = RemoteRequestValidator.validateRecord(build, json.getAsJsonObject());
            if (null != problem) throw new EruptApiErrorTip(problem);
        }
    }

    // What the server established about a forwarded call: the body it read, the user's rights, the checks it ran,
    // and what it completes in the answer
    private record Forward(byte[] body, PowerObject userPower, Set<CloudSignature.Enforced> enforced, EruptRouter.Resolve resolve) {
    }

    // A node may send a field's JSON with only what it declared: the attributes it leaves out get the defaults a
    // local erupt's build carries, so the UI finds every array and sub-type it reads
    private static void completeFields(EruptBuildModel build) {
        List<EruptModel> models = new ArrayList<>();
        models.add(build.getEruptModel());
        Optional.ofNullable(build.getOperationErupts()).ifPresent(it -> models.addAll(it.values()));
        Optional.ofNullable(build.getCombineErupts()).ifPresent(it -> models.addAll(it.values()));
        Optional.ofNullable(build.getTabErupts()).ifPresent(it -> it.values().forEach(EruptCloudServerInterceptor::completeFields));
        for (EruptModel model : models) {
            for (EruptFieldModel field : Optional.ofNullable(model.getEruptFieldModels()).orElse(List.of())) {
                if (null != field.getEruptFieldJson()) {
                    field.setEruptFieldJson(AnnotationProcess.completeJson(EruptField.class, field.getEruptFieldJson()));
                }
            }
        }
    }

    private void eruptBuildProcess(EruptBuildModel eruptBuildModel, String nodeName) {
        String prefix = nodeName + EruptConst.DOT;
        completeFields(eruptBuildModel);
        eruptBuildModel.getEruptModel().setEruptName(prefix + eruptBuildModel.getEruptModel().getEruptName());
        //Modify the Drill value
        JsonArray drills = eruptBuildModel.getEruptModel().getEruptJson().getAsJsonArray(LambdaSee.method(Erupt::drills));
        if (!drills.isEmpty()) {
            String linkErupt = LambdaSee.method(Link::linkErupt);
            for (JsonElement drill : drills) {
                JsonObject link = drill.getAsJsonObject().get(LambdaSee.method(Drill::link)).getAsJsonObject();
                link.addProperty(linkErupt, prefix + link.get(linkErupt).getAsString());
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
