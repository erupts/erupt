package xyz.erupt.security.interceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.upms.annotation.EruptLoginAuth;
import xyz.erupt.upms.annotation.EruptMenuAuth;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

import java.io.IOException;

/**
 * @author YuePeng
 * date 2023/6/10 22:42
 */
@Component
public class EruptSuperInterceptor implements AsyncHandlerInterceptor {

    @Resource
    private EruptSessionService sessionService;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private I18nTranslate i18nTranslate;


    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull Object handler) throws IOException {
        if (handler instanceof HandlerMethod handlerMethod) {
            // Mapped on /**, so every controller call passes through here: pin the language now
            // and anything downstream reads it without having to hold on to the request. Async
            // work (SSE generation, tool execution) gets it with the MetaContext it restores.
            MetaContext.registerLang(i18nTranslate.currentLang());
            EruptLoginAuth eruptAuth = handlerMethod.getMethodAnnotation(EruptLoginAuth.class);
            EruptMenuAuth eruptMenuAuth = handlerMethod.getMethodAnnotation(EruptMenuAuth.class);
            if (null != eruptAuth || null != eruptMenuAuth) {
                String token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
                if (null == token) {
                    token = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_TOKEN);
                }
                if (null == token || null == sessionService.get(SessionKey.TOKEN_OLINE + token)) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.sendError(HttpStatus.UNAUTHORIZED.value());
                    return false;
                }
                MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfo();
                MetaContext.registerToken(token);
                MetaContext.register(new MetaUser(metaUserinfo.getId(), metaUserinfo.getAccount(), metaUserinfo.getUsername(), metaUserinfo.getTenantId()));
            }
            if (null != eruptMenuAuth) {
                if (null == eruptUserService.getEruptMenuByValue(eruptMenuAuth.value())) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.sendError(HttpStatus.FORBIDDEN.value());
                    return false;
                }
            }
        }
        return true;
    }

    // MetaContext is a thread local and Tomcat reuses its workers. EruptSecurityInterceptor clears
    // it for /erupt-api only, so without this a caller outside it (MCP, @EruptLoginAuth APIs) left
    // its identity, tenant included, to the next request on the thread, whose Hibernate session
    // resolves its tenant from MetaContext before any interceptor has run
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        MetaContext.remove();
    }

    // An async request (SSE) hands its worker back here instead of through afterCompletion; the
    // async work holds its own copy of the context, so the worker can drop it all the same
    @Override
    public void afterConcurrentHandlingStarted(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        MetaContext.remove();
    }

}
