package xyz.erupt.upms.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.model.EruptMenu;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author YuePeng
 * date 2021/8/20 00:41
 */
@Service
public class EruptContextService {

    @Resource
    private HttpServletRequest request;

    @Resource
    private EruptSessionService sessionService;

    //获取erupt上下文对象
    public Class<?> getContextEruptClass() {
        return EruptCoreService.getErupt(MetaContext.get().getMetaErupt().getName()).getClazz();
    }

    //获取当前请求token
    public String getCurrentToken() {
        String token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
        if (StringUtils.isBlank(token)) {
            return request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_TOKEN);
        }
        return token;
    }

    //获取当前菜单对象
    public EruptMenu getCurrentEruptMenu() {
        return sessionService.getMapValue(SessionKey.MENU_VALUE_MAP + getCurrentToken()
                , MetaContext.get().getMetaErupt().getName(), EruptMenu.class);
    }

}
