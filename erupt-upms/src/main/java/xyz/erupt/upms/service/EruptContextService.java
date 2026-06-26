package xyz.erupt.upms.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.model.EruptMenu;

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

    //Get the current request token
    public String getCurrentToken() {
        String token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
        return StringUtils.isNotBlank(token) ? token : request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_TOKEN);
    }

    //Get the current menu object
    public EruptMenu getCurrentEruptMenu() {
        MetaErupt metaErupt = MetaContext.getErupt();
        return sessionService.getMapValue(SessionKey.MENU_VALUE_MAP + getCurrentToken()
                , (metaErupt.getMenuValue() == null ? metaErupt.getName() : metaErupt.getMenuValue()).toLowerCase(),
                EruptMenu.class);
    }

}
