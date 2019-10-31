package xyz.erupt.auth.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import xyz.erupt.auth.service.UserService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.service.CoreService;

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

    public static final String ERUPT_HEADER_TOKEN = "token";

    private static final String PARENT_ERUPT_HEADER_KEY = "parent_erupt";

    private static final String URL_ERUPT_PARAM_KEY = "_erupt";

    private static final String PARENT_ERUPT_PARAM_KEY = "_parent_erupt";

    private static final String URL_ERUPT_PARAM_TOKEN = "_token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        EruptRouter eruptRouter = null;
        if (handler instanceof HandlerMethod) {
            eruptRouter = ((HandlerMethod) handler).getMethodAnnotation(EruptRouter.class);
        }
        if (null == eruptRouter) {
            return true;
        }
        String token = null;
        String eruptName = null;
        String parentEruptName = null;
        if (eruptRouter.verifyMethod() == EruptRouter.VerifyMethod.HEADER) {
            token = request.getHeader(ERUPT_HEADER_TOKEN);
            eruptName = request.getHeader(ERUPT_HEADER_KEY);
            parentEruptName = request.getHeader(PARENT_ERUPT_HEADER_KEY);
        } else if (eruptRouter.verifyMethod() == EruptRouter.VerifyMethod.PARAM) {
            token = request.getParameter(URL_ERUPT_PARAM_TOKEN);
            eruptName = request.getParameter(URL_ERUPT_PARAM_KEY);
            parentEruptName = request.getHeader(PARENT_ERUPT_PARAM_KEY);
        }
        if (null == token || !userService.verifyToken(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        String path = request.getServletPath();
        //权限校验
        if (eruptRouter.verifyErupt()) {
            EruptModel eruptModel = CoreService.getErupt(eruptName);
            //TODO 通过请求头parent_erupt来控制权限
//            if (StringUtils.isNotBlank(parentEruptName)){
//                CoreService.getErupt(parentEruptName);
//            }
            if (null == eruptModel) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return false;
            }
            String authStr = path.split("/")[eruptRouter.startAuthIndex() + eruptRouter.authIndex() + 1];
            if (!path.contains(eruptName) || !userService.verifyMenuLimit(token, authStr, eruptModel)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return false;
            }
        }
        return true;
    }


}
