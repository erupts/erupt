package xyz.erupt.security.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.security.config.EruptSecurityProp;
import xyz.erupt.security.tl.RequestBodyTL;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.log.EruptOperateLog;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.util.IpUtil;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2022/7/10 21:39
 */
@Service
public class OperationService {

    @Resource
    private EntityManager entityManager;

    @Resource
    private EruptSecurityProp eruptSecurityProp;

    @Resource
    private HttpServletRequest request;

    @Resource
    private EruptContextService eruptContextService;

    @Transactional
    public void record(Object handler, Exception ex) {
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
                        String errorInfo = ExceptionUtils.getStackTrace(e);
                        operate.setErrorInfo(errorInfo.length() > 4000 ? errorInfo.substring(0, 4000) : errorInfo);
                        operate.setStatus(false);
                    });
                    Object param = RequestBodyTL.get().getBody();
                    operate.setReqParam(null == param ? findRequestParamVal(request) : param.toString());
                    RequestBodyTL.remove();
                    entityManager.persist(operate);
                });
            }
        }
    }

    private String findRequestParamVal(HttpServletRequest request) {
        if (request.getParameterMap().size() > 0) {
            StringBuilder sb = new StringBuilder();
            request.getParameterMap().forEach((key, value) -> sb.append(key).append("=").append(Arrays.toString(value)).append("\n"));
            return sb.toString();
        }
        return null;
    }

}
