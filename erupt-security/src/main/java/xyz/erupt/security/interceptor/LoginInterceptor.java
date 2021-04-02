package xyz.erupt.security.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.security.config.EruptSecurityProp;
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
import java.util.Date;

/**
 * @author YuePeng
 * date 2019-12-05.
 */
@Service
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EntityManager entityManager;

    @Resource
    private EruptSecurityProp eruptSecurityProp;

    static final String REQ_BODY = "@req_body@";

    private static final String ERUPT_PARENT_HEADER_KEY = "eruptParent";

    private static final String ERUPT_PARENT_PARAM_KEY = "_eruptParent";
    private static final String REQ_DATE = "@req_date@";
    @Resource
    private EruptSessionService sessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        request.setAttribute(REQ_DATE, System.currentTimeMillis());
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
                EruptRecordOperate eruptOperate = handlerMethod.getMethodAnnotation(EruptRecordOperate.class);
                if (null != eruptOperate) {
                    EruptRouter eruptRouter = handlerMethod.getMethodAnnotation(EruptRouter.class);
                    EruptOperateLog operate = new EruptOperateLog();
                    if (null != eruptRouter && eruptRouter.verifyType() == EruptRouter.VerifyType.ERUPT) {
                        String eruptName;
                        if (eruptRouter.verifyMethod() == EruptRouter.VerifyMethod.HEADER) {
                            eruptName = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_KEY);
                        } else {
                            eruptName = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_KEY);
                        }
                        operate.setApiName(eruptOperate.desc() + " | " + EruptCoreService.getErupt(eruptName).getErupt().name());
                    } else {
                        operate.setApiName(eruptOperate.desc());
                    }
                    Object param = request.getAttribute(REQ_BODY);
                    if (null != param) {
                        operate.setReqParam(param.toString());
                    }
                    operate.setIp(IpUtil.getIpAddr(request));
                    operate.setRegion(IpUtil.getCityInfo(operate.getIp()));
                    operate.setStatus(true);
                    operate.setReqMethod(request.getMethod());
                    operate.setReqAddr(request.getRequestURL().toString());
                    operate.setEruptUser(new EruptUser() {{
                        this.setId(eruptUserService.getCurrentUid());
                    }});
                    Date date = new Date();
                    operate.setCreateTime(date);
                    operate.setTotalTime(date.getTime() - (Long) request.getAttribute(REQ_DATE));
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
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        super.afterConcurrentHandlingStarted(request, response, handler);
    }
}
