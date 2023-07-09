package xyz.erupt.security.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.security.service.OperationService;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author YuePeng
 * date 2019-12-05.
 */
@Service
@Order
public class EruptSecurityInterceptor implements AsyncHandlerInterceptor {

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptProp eruptProp;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private OperationService operationService;

    private static final String ERUPT_PARENT_HEADER_KEY = "eruptParent";

    private static final String ERUPT_PARENT_PARAM_KEY = "_eruptParent";

    @Resource
    private EruptSessionService sessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        EruptRouter eruptRouter = null;
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            eruptRouter = handlerMethod.getMethodAnnotation(EruptRouter.class);
        }
        if (null == eruptRouter) return true;
        String token = null;
        String eruptName = null;
        String parentEruptName = null;
        if (eruptRouter.verifyMethod() == EruptRouter.VerifyMethod.HEADER) {
            token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
            eruptName = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_KEY);
            parentEruptName = request.getHeader(ERUPT_PARENT_HEADER_KEY);
        } else if (eruptRouter.verifyMethod() == EruptRouter.VerifyMethod.PARAM) {
            token = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_TOKEN);
            eruptName = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_KEY);
            parentEruptName = request.getHeader(ERUPT_PARENT_PARAM_KEY);
        }
        if (eruptRouter.verifyType().equals(EruptRouter.VerifyType.ERUPT)) {
            MetaContext.register(new MetaErupt(eruptName, eruptName));
            EruptModel erupt = EruptCoreService.getErupt(eruptName);
            if (null == erupt) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return false;
            }
            if (!erupt.getErupt().authVerify()) return true;
        }
        if (null == token || null == sessionService.get(SessionKey.TOKEN_OLINE + token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfo();
        MetaContext.registerToken(token);
        MetaContext.register(new MetaUser(metaUserinfo.getId() + "", metaUserinfo.getAccount(), metaUserinfo.getUsername()));
        //权限校验
        String authStr = request.getServletPath().split("/")[eruptRouter.skipAuthIndex() + eruptRouter.authIndex()];
        switch (eruptRouter.verifyType()) {
            case LOGIN:
                break;
            case MENU:
                if (!eruptRouter.verifyHandler().isInterface()) {
                    authStr = EruptSpringUtil.getBean(eruptRouter.verifyHandler()).convertAuthStr(eruptRouter, request, authStr);
                }
                if (null == eruptUserService.getEruptMenuByValue(authStr)) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.sendError(HttpStatus.FORBIDDEN.value());
                    return false;
                }
                MetaContext.register(new MetaErupt(null, authStr));
                break;
            case ERUPT:
                EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
                $ep:
                if (StringUtils.isNotBlank(parentEruptName)) {
                    EruptModel eruptParentModel = EruptCoreService.getErupt(parentEruptName);
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
                        if (void.class != operation.eruptClass()) {
                            if (eruptModel.getEruptName().equals(operation.eruptClass().getSimpleName())) {
                                authStr = eruptParentModel.getEruptName();
                                eruptModel = eruptParentModel;
                                break $ep;
                            }
                        }
                    }
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    return false;
                }
                if (!authStr.equalsIgnoreCase(eruptModel.getEruptName())) {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    return false;
                }
                if (null == eruptUserService.getEruptMenuByValue(eruptModel.getEruptName())) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.sendError(HttpStatus.FORBIDDEN.value());
                    return false;
                }
                break;
        }
        if (eruptProp.isRedisSessionRefresh()) {
            for (String uk : SessionKey.USER_KEY_GROUP) {
                sessionService.expire(uk + token, eruptUpmsProp.getExpireTimeByLogin(), TimeUnit.MINUTES);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            operationService.record(handler, ex);
        } catch (Exception e) {
            //TODO 失败后抛出异常（版本兼容原因未抛出）
            e.printStackTrace();
        } finally {
            MetaContext.remove();
        }
    }

}
