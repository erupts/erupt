package xyz.erupt.cloud.server.config;

import cn.hutool.http.*;
import lombok.SneakyThrows;
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
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.net.ConnectException;
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
            if (null == token || null == eruptSessionService.get(SessionKey.USER_TOKEN + token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.sendError(HttpStatus.UNAUTHORIZED.value());
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
            HttpResponse httpResponse = this.httpProxy(request, metaNode, request.getRequestURI().replace(erupt, eruptName), eruptName);
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
                eruptSecurityInterceptor.afterCompletion(request, response, handler, new Exception(httpResponse.body()));
            } else {
                eruptSecurityInterceptor.afterCompletion(request, response, handler, null);
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

    @SneakyThrows
    public HttpResponse httpProxy(HttpServletRequest request, MetaNode metaNode, String path, String eruptName) {
        String location;
        if (null != metaNode.getManualLocations()) {
            location = metaNode.getManualLocations().toArray(new String[0])[metaNode.getCount() % metaNode.getManualLocations().size()];
        } else {
            location = metaNode.getLocations().toArray(new String[0])[metaNode.getCount() % metaNode.getLocations().size()];
        }
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        headers.put(CloudCommonConst.ACCESS_TOKEN, metaNode.getAccessToken());
        headers.put(EruptMutualConst.TOKEN, eruptContextService.getCurrentToken());
        headers.put(EruptMutualConst.ERUPT, eruptName);
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
        } catch (ConnectException | HttpException e) {
            throw new EruptWebApiRuntimeException(location + " -> " + e.getMessage());
        }
    }

    private void eruptBuildProcess(EruptBuildModel eruptBuildModel, String nodeName) {
        String prefix = nodeName + ".";
        eruptBuildModel.getEruptModel().setEruptName(prefix + eruptBuildModel.getEruptModel().getEruptName());
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
