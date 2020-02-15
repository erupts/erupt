package xyz.erupt.auth.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.auth.service.EruptUserService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.bean.EruptFieldModel;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.service.CoreService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author liyuepeng
 * @date 2019-12-05.
 */
@Service
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    public EruptUserService eruptUserService;

    private static final String ERUPT_HEADER_KEY = "erupt";

    public static final String ERUPT_HEADER_TOKEN = "token";

    private static final String ERUPT_PARENT_HEADER_KEY = "eruptParent";

    private static final String URL_ERUPT_PARAM_KEY = "_erupt";

    private static final String ERUPT_PARENT_PARAM_KEY = "_eruptParent";

    public static final String URL_ERUPT_PARAM_TOKEN = "_token";

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
            parentEruptName = request.getHeader(ERUPT_PARENT_HEADER_KEY);
        } else if (eruptRouter.verifyMethod() == EruptRouter.VerifyMethod.PARAM) {
            token = request.getParameter(URL_ERUPT_PARAM_TOKEN);
            eruptName = request.getParameter(URL_ERUPT_PARAM_KEY);
            parentEruptName = request.getHeader(ERUPT_PARENT_PARAM_KEY);
        }
        if (null == token || !eruptUserService.verifyToken(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        String path = request.getServletPath();
        //权限校验
        switch (eruptRouter.verifyType()) {
            case LOGIN:
                break;
            case MENU:
                boolean bool = eruptUserService.verifyMenuAuth(token, eruptName);
                if (!bool) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    return false;
                }
                break;
            case ERUPT:
                EruptModel eruptModel = CoreService.getErupt(eruptName);
                if (null == eruptModel) {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    return false;
                }
                String authStr = path.split("/")[eruptRouter.skipAuthIndex() + eruptRouter.authIndex() + 1];
                //eruptParent logic
                $ep:
                if (StringUtils.isNotBlank(parentEruptName)) {
                    EruptModel eruptParentModel = CoreService.getErupt(parentEruptName);
                    for (EruptFieldModel model : eruptParentModel.getEruptFieldModels()) {
                        if (eruptModel.getEruptName().equals(model.getFieldReturnName())) {
                            if (authStr.equals(eruptModel.getEruptName())) {
                                authStr = eruptParentModel.getEruptName();
                            }
                            eruptModel = eruptParentModel;
                            break $ep;
                        }
                    }
                    for (RowOperation operation : eruptParentModel.getErupt().rowOperation()) {
                        if (operation.eruptClass().getSimpleName().equals(eruptModel.getEruptName())) {
                            break $ep;
                        }
                    }
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    return false;
                }
                if (!path.contains(eruptName) || !eruptUserService.verifyEruptMenuAuth(token, authStr, eruptModel)) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    return false;
                }
                break;
        }
        return true;
    }


}
