package xyz.erupt.security.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.security.config.EruptSecurityProp;
import xyz.erupt.security.tl.RequestBodyTL;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.log.EruptOperateLog;
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
import java.util.Map;

/**
 * @author YuePeng
 * date 2019-12-05.
 */
@Service
public class EruptSecurityInterceptor extends WebRequestHandlerInterceptorAdapter {

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EntityManager entityManager;

    @Resource
    private EruptSecurityProp eruptSecurityProp;

    private static final String ERUPT_PARENT_HEADER_KEY = "eruptParent";

    private static final String ERUPT_PARENT_PARAM_KEY = "_eruptParent";

    @Resource
    private EruptSessionService sessionService;

    public EruptSecurityInterceptor(WebRequestInterceptor requestInterceptor) {
        super(requestInterceptor);
    }

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

                if (!authStr.equals(eruptModel.getEruptName())) {
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
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    @Transactional
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (eruptSecurityProp.isRecordOperateLog()) {
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                EruptRecordOperate eruptRecordOperate = handlerMethod.getMethodAnnotation(EruptRecordOperate.class);
                if (null != eruptRecordOperate) {
                    EruptOperateLog operate = new EruptOperateLog();
                    if (eruptRecordOperate.dynamicConfig().isInterface()) {
                        operate.setApiName(eruptRecordOperate.value());
                    } else {
                        String eruptName = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_KEY);
                        if (null == eruptName) {
                            eruptName = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_KEY);
                        }
                        EruptRecordOperate.DynamicConfig dynamicConfig = EruptSpringUtil.getBean(eruptRecordOperate.dynamicConfig());
                        if (!dynamicConfig.canRecord(eruptName, handlerMethod.getMethod())) return;
                        operate.setApiName(dynamicConfig.naming(eruptRecordOperate.value(), eruptName, handlerMethod.getMethod()));
                    }
                    operate.setIp(IpUtil.getIpAddr(request));
                    operate.setRegion(IpUtil.getCityInfo(operate.getIp()));
                    operate.setStatus(true);
                    operate.setReqMethod(request.getMethod());
                    operate.setReqAddr(request.getRequestURL().toString());
                    if (null != eruptUserService.getCurrentUid()) {
                        operate.setEruptUser(new EruptUser() {{
                            this.setId(eruptUserService.getCurrentUid());
                        }});
                    }
                    Object param = RequestBodyTL.get().getBody();
                    if (null != param) {
                        operate.setReqParam(param.toString());
                    } else {
                        operate.setReqParam(findRequestParamVal(request));
                    }
                    operate.setCreateTime(new Date());
                    operate.setTotalTime((int) (operate.getCreateTime().getTime() - RequestBodyTL.get().getDate()));
                    RequestBodyTL.remove();
                    if (null != ex) {
                        operate.setErrorInfo(ExceptionUtils.getStackTrace(ex));
                        operate.setStatus(false);
                    }
                    entityManager.persist(operate);
                }
            }
        }
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) {
        super.afterConcurrentHandlingStarted(request, response, handler);
    }

    public String findRequestParamVal(HttpServletRequest request) {
        if (request.getParameterMap().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                sb.append(entry.getKey()).append("=").append(Arrays.toString(entry.getValue())).append("\n");
            }
            return sb.toString();
        }
        return null;
    }
}
