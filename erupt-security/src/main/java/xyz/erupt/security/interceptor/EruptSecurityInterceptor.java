package xyz.erupt.security.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.annotation.EruptRecordOperate;
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
import xyz.erupt.security.config.EruptSecurityProp;
import xyz.erupt.security.tl.RequestBodyTL;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.log.EruptOperateLog;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.util.IpUtil;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

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
    private EntityManager entityManager;

    @Resource
    private EruptProp eruptProp;

    @Resource
    private EruptSecurityProp eruptSecurityProp;

    @Resource
    private EruptContextService eruptContextService;

    private static final String ERUPT_PARENT_HEADER_KEY = "eruptParent";

    private static final String ERUPT_PARENT_PARAM_KEY = "_eruptParent";

    @Resource
    private EruptSessionService sessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
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
            token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
            eruptName = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_KEY);
            parentEruptName = request.getHeader(ERUPT_PARENT_HEADER_KEY);
        } else if (eruptRouter.verifyMethod() == EruptRouter.VerifyMethod.PARAM) {
            token = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_TOKEN);
            eruptName = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_KEY);
            parentEruptName = request.getHeader(ERUPT_PARENT_PARAM_KEY);
        }
        if (eruptRouter.verifyType().equals(EruptRouter.VerifyType.ERUPT)) {
            MetaContext.register(new MetaErupt(eruptName));
            EruptModel erupt = EruptCoreService.getErupt(eruptName);
            if (null == erupt) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return false;
            }
            if (!erupt.getErupt().authVerify()) {
                return true;
            }
        }

        if (null == token || null == sessionService.get(SessionKey.USER_TOKEN + token)) {
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
                if (null == eruptUserService.getEruptMenuByValue(authStr)) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.sendError(HttpStatus.FORBIDDEN.value());
                    return false;
                }
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
            sessionService.expire(SessionKey.USER_TOKEN + token);
            sessionService.expire(SessionKey.MENU_VIEW + token);
            sessionService.expireMap(SessionKey.MENU_VALUE_MAP + token);
        }
        return true;
    }

    @Override
    @Transactional
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            if (eruptSecurityProp.isRecordOperateLog()) {
                if (handler instanceof HandlerMethod) {
                    HandlerMethod handlerMethod = (HandlerMethod) handler;
                    Optional.ofNullable(handlerMethod.getMethodAnnotation(EruptRecordOperate.class)).ifPresent(eruptRecordOperate -> {
                        EruptOperateLog operate = new EruptOperateLog();
                        if (eruptRecordOperate.dynamicConfig().isInterface()) {
                            operate.setApiName(eruptRecordOperate.value());
                        } else {
                            String eruptName = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_KEY);
                            eruptName = Optional.ofNullable(eruptName).orElse(request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_KEY));
                            EruptRecordOperate.DynamicConfig dynamicConfig = EruptSpringUtil.getBean(eruptRecordOperate.dynamicConfig());
                            if (!dynamicConfig.canRecord(eruptName, handlerMethod.getMethod())) return;
                            operate.setApiName(dynamicConfig.naming(eruptRecordOperate.value(),
                                    Optional.ofNullable(eruptContextService.getCurrentEruptMenu()).orElse(new EruptMenu()).getName(),
                                    eruptName, handlerMethod.getMethod()));
                        }
                        operate.setIp(IpUtil.getIpAddr(request));
                        operate.setRegion(IpUtil.getCityInfo(operate.getIp()));
                        operate.setStatus(true);
                        operate.setReqMethod(request.getMethod());
                        operate.setReqAddr(request.getRequestURL().toString());
                        operate.setOperateUser(MetaContext.getUser().getName());
                        operate.setCreateTime(new Date());
                        operate.setTotalTime(operate.getCreateTime().getTime() - RequestBodyTL.get().getDate());
                        Optional.ofNullable(ex).ifPresent(e -> {
                            operate.setErrorInfo(ExceptionUtils.getStackTrace(e));
                            operate.setStatus(false);
                        });
                        Object param = RequestBodyTL.get().getBody();
                        operate.setReqParam(null == param ? findRequestParamVal(request) : param.toString());
                        RequestBodyTL.remove();
                        entityManager.persist(operate);
                    });
                }
            }
        } finally {
            MetaContext.remove();
        }
    }

    public String findRequestParamVal(HttpServletRequest request) {
        if (request.getParameterMap().size() > 0) {
            StringBuilder sb = new StringBuilder();
            request.getParameterMap().forEach((key, value) -> sb.append(key).append("=").append(Arrays.toString(value)).append("\n"));
            return sb.toString();
        }
        return null;
    }
}
