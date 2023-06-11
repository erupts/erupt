package xyz.erupt.cloud.server.interceptor;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.cloud.server.node.MetaNode;
import xyz.erupt.cloud.server.node.NodeContext;
import xyz.erupt.cloud.server.node.NodeManager;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.security.interceptor.EruptSecurityInterceptor;
import xyz.erupt.security.service.OperationService;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns(EruptRestPath.ERUPT_API + "/**");
    }

    private static final String[] TRANSFER_HEADERS = {
            "Content-Disposition"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        EruptRouter eruptRouter = null;
        if (handler instanceof HandlerMethod) {
            eruptRouter = ((HandlerMethod) handler).getMethodAnnotation(EruptRouter.class);
        }
        if (null == eruptRouter) return true;
        if (EruptRouter.VerifyType.ERUPT == eruptRouter.verifyType()) {
            String erupt = request.getHeader(EruptMutualConst.ERUPT);
            if (erupt == null) {
                erupt = request.getParameter("_" + EruptMutualConst.ERUPT);
            }
            if (erupt == null) {
                return true;
            }
            if (!erupt.contains(".")) return true;
            if (null != EruptCoreService.getErupt(erupt)) return true;
            String token = eruptContextService.getCurrentToken();
            if (null == token || null == eruptSessionService.get(SessionKey.TOKEN_OLINE + token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.sendError(HttpStatus.UNAUTHORIZED.value());
                return false;
            }
            if (null == eruptUserService.getEruptMenuByValue(erupt)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.sendError(HttpStatus.FORBIDDEN.value());
                return false;
            }
            int point = erupt.lastIndexOf(".");
            String nodeName = erupt.substring(0, point);
            String eruptName = erupt.substring(point + 1);
            MetaNode metaNode = nodeManager.getNode(nodeName);
            NodeContext.set(metaNode);
            if (null == metaNode) {
                throw new EruptWebApiRuntimeException("'" + nodeName + "' node not ready");
            }
            MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfo();
            MetaContext.register(new MetaUser(metaUserinfo.getId() + "", metaUserinfo.getAccount(), metaUserinfo.getUsername()));
            MetaContext.register(new MetaErupt(erupt));
            MetaContext.registerToken(token);
            String path = request.getRequestURI().replace(erupt, eruptName);
            HttpResponse httpResponse = this.httpProxy(request, metaNode, path.substring(path.indexOf(EruptRestPath.ERUPT_API)), eruptName);
            Optional.ofNullable(httpResponse.header("Content-Type")).ifPresent(response::setContentType);
            for (String transferHeader : TRANSFER_HEADERS) {
                Optional.ofNullable(httpResponse.header(transferHeader)).ifPresent(it -> response.addHeader(transferHeader, it));
            }
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            if (httpResponse.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                if (this.isSimpleJson(httpResponse.body())) {
                    throw new EruptWebApiRuntimeException(GsonFactory.getGson().fromJson(httpResponse.body(), Map.class), false);
                } else {
                    throw new EruptWebApiRuntimeException(httpResponse.body(), false);
                }
            }
            if (httpResponse.getStatus() != HttpStatus.OK.value()) {
                response.sendError(httpResponse.getStatus());
                operationService.record(handler, new Exception(httpResponse.body()));
            } else {
                operationService.record(handler, null);
            }
            if ((EruptRestPath.ERUPT_BUILD + "/" + erupt).equals(request.getServletPath())) {
                EruptBuildModel eruptBuildModel = GsonFactory.getGson().fromJson(httpResponse.body(), EruptBuildModel.class);
                this.eruptBuildProcess(eruptBuildModel, nodeName);
                response.getOutputStream().write(GsonFactory.getGson().toJson(eruptBuildModel).getBytes(StandardCharsets.UTF_8));
            } else {
                response.getOutputStream().write(httpResponse.bodyBytes());
            }
            NodeContext.remove();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        eruptSecurityInterceptor.afterConcurrentHandlingStarted(request, response, handler);
        NodeContext.remove();
    }

    private boolean isSimpleJson(String str) {
        if (StringUtils.isBlank(str)) return false;
        str = str.trim();
        return str.startsWith("{") && str.endsWith("}");
    }

    private int count = 0;

    public HttpResponse httpProxy(HttpServletRequest request, MetaNode metaNode, String path, String eruptName) {
        if (count >= Integer.MAX_VALUE) {
            count = 0;
        }
        String location = metaNode.getLocations().toArray(new String[0])[metaNode.getLocations().size() <= 1 ? 0 : (count++ % metaNode.getLocations().size())];
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        headers.remove("host");
        headers.put(CloudCommonConst.HEADER_ACCESS_TOKEN, metaNode.getAccessToken());
        headers.put(EruptMutualConst.TOKEN, eruptContextService.getCurrentToken());
        headers.put(EruptMutualConst.ERUPT, eruptName);
        headers.put(EruptMutualConst.USER, Base64Encoder.encode(GsonFactory.getGson().toJson(MetaContext.getUser())));
        HttpRequest httpRequest = HttpUtil.createRequest(Method.valueOf(request.getMethod()), location + path + (null == request.getQueryString() ? "" : "?" + request.getQueryString()));
        try {
            if (null != request.getContentType() && request.getContentType().contains("multipart/form-data")) {
                for (Part part : request.getParts()) {
                    httpRequest.form(part.getName(), StreamUtils.copyToByteArray(part.getInputStream()), part.getSubmittedFileName());
                }
            } else {
                httpRequest.body(StreamUtils.copyToByteArray(request.getInputStream()));
            }
            HttpResponse httpResponse = httpRequest.addHeaders(headers).execute();
            if (httpResponse.getStatus() != HttpStatus.OK.value()) {
                log.warn("{} -> {}", location + path, httpResponse.body());
            }
            return httpResponse;
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(location + " -> " + e.getMessage());
        }
    }

    private void eruptBuildProcess(EruptBuildModel eruptBuildModel, String nodeName) {
        String prefix = nodeName + ".";
        eruptBuildModel.getEruptModel().setEruptName(prefix + eruptBuildModel.getEruptModel().getEruptName());
        //修改Drill的值
        JsonArray drills = eruptBuildModel.getEruptModel().getEruptJson().getAsJsonArray("drills");
        if (drills.size() > 0) {
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
