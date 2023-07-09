package xyz.erupt.security.interceptor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import xyz.erupt.upms.annotation.EruptLoginAuth;
import xyz.erupt.upms.annotation.EruptMenuAuth;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            EruptLoginAuth eruptAuth = handlerMethod.getMethodAnnotation(EruptLoginAuth.class);
            EruptMenuAuth eruptMenuAuth = handlerMethod.getMethodAnnotation(EruptMenuAuth.class);
            if (null != eruptAuth || null != eruptMenuAuth) {
                String token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
                if (null == token || null == sessionService.get(SessionKey.TOKEN_OLINE + token)) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.sendError(HttpStatus.UNAUTHORIZED.value());
                    return false;
                }
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

}
