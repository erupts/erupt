package xyz.erupt.eruptlimit.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.eruptlimit.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by liyuepeng on 12/5/18.
 */
@Service
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    public UserService userService;

    private static final String ERUPT_HEADER_KEY = "erupt";

    private static final String ERUPT_HEADER_TOKEN = "token";

    private static final String URL_ERUPT_PARAM_KEY = "_erupt";

    private static final String URL_ERUPT_PARAM_TOKEN = "_token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        userService.getRequestPath(((HandlerMethod) handler));
        EruptRouter eruptRouter = ((HandlerMethod) handler).getMethodAnnotation(EruptRouter.class);
        if (null == eruptRouter) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            return false;
        }
        if (eruptRouter.loginVerify()) {
            String token = null;
            String eruptName = null;
            if (eruptRouter.verifyMethod() == EruptRouter.VerifyMethod.HEADER) {
                token = request.getHeader(ERUPT_HEADER_TOKEN);
                eruptName = request.getHeader(ERUPT_HEADER_KEY);
            } else if (eruptRouter.verifyMethod() == EruptRouter.VerifyMethod.PARAM) {
                token = request.getParameter(URL_ERUPT_PARAM_TOKEN);
                eruptName = request.getParameter(URL_ERUPT_PARAM_KEY);
            }
            if (null == token || !userService.verifyToken(token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            }
            String path = request.getServletPath();
            //权限校验
            if (path.startsWith(RestPath.ERUPT_API)) {
                EruptModel eruptModel = CoreService.getErupt(eruptName);
                if (null == eruptModel) {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    return false;
                }
                if (!path.contains(eruptName) || !userService.verifyMenuLimit(token, path, eruptModel)) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        EruptRouter eruptRouter = ((HandlerMethod) handler).getMethodAnnotation(EruptRouter.class);
        if (null != eruptRouter) {
            if (eruptRouter.base64()) {
//                new BASE64Encoder().encode();
            }
        }
        super.postHandle(request, response, handler, modelAndView);
    }

}
