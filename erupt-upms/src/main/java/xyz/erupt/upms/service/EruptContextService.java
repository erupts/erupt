package xyz.erupt.upms.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.core.constant.EruptReqHeader;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.model.EruptMenu;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

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
    @Deprecated
    public Class<?> getContextEruptClass() {
        return EruptCoreService.getErupt(MetaContext.getErupt().getName()).getClazz();
    }

    //获取当前请求token
    public String getCurrentToken() {
        String token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
        return StringUtils.isNotBlank(token) ? token : request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_TOKEN);
    }

    //获取当前菜单对象
    public EruptMenu getCurrentEruptMenu() {
        MetaErupt metaErupt = MetaContext.getErupt();
        return sessionService.getMapValue(SessionKey.MENU_VALUE_MAP + getCurrentToken()
                , (metaErupt.getMenuValue() == null ? metaErupt.getName() : metaErupt.getMenuValue()).toLowerCase(),
                EruptMenu.class);
    }

    public String getDrillValue() {
        return request.getHeader(EruptReqHeader.DRILL_VALUE);
    }

    public String getDrillSourceErupt() {
        return request.getHeader(EruptReqHeader.DRILL_SOURCE_ERUPT);
    }

}
