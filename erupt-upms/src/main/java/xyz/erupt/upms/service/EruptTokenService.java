package xyz.erupt.upms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.vo.EruptMenuVo;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author YuePeng
 * date 2024/10/7 16:01
 */
@Service
@Slf4j
public class EruptTokenService {

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptSessionService eruptSessionService;

    @Resource
    private EruptProp eruptProp;

    @Resource
    private HttpServletRequest request;

    public void loginToken(MetaUserinfo metaUserinfo, List<MetaMenu> metaMenus, String token, Integer tokenExpire) {
        // Map<String, EruptMenu>
        Map<String, Object> eruptMenuMap = new HashMap<>();
        List<EruptMenuVo> eruptMenuVos = new ArrayList<>();
        metaMenus.stream().sorted(Comparator.comparing(MetaMenu::getSort, Comparator.nullsFirst(Integer::compareTo))).forEach(menu -> {
            if (menu.getStatus() != MenuStatus.DISABLE) {
                if (null != menu.getValue()) {
                    //根据URL规则?后的均是参数如：eruptTest?code=test，把参数 ?code=test 去除
                    eruptMenuMap.put(menu.getValue().toLowerCase().split("\\?")[0], EruptMenu.fromMetaMenu(menu));
                }
                if (menu.getStatus() == MenuStatus.OPEN) {
                    eruptMenuVos.add(EruptMenuVo.fromMetaMenu(menu));
                }
            }
        });
        eruptSessionService.putMap(SessionKey.MENU_VALUE_MAP + token, eruptMenuMap, tokenExpire);
        eruptSessionService.put(SessionKey.MENU_VIEW + token, GsonFactory.getGson().toJson(eruptMenuVos), tokenExpire);
        eruptSessionService.put(SessionKey.USER_INFO + token, GsonFactory.getGson().toJson(metaUserinfo), tokenExpire);
        eruptSessionService.put(SessionKey.TOKEN_OLINE + token, metaUserinfo.getAccount(), tokenExpire);
    }

    public boolean tokenExist(String token) {
        return eruptSessionService.exist(SessionKey.TOKEN_OLINE + token);
    }

    public void loginToken(EruptUser eruptUser, String token, Integer tokenExpire) {
        List<MetaMenu> metaMenus = new ArrayList<>();
        List<EruptMenu> eruptMenus = EruptSpringUtil.getBean(EruptMenuService.class).getUserAllMenu(eruptUser);
        eruptMenus.forEach(menu -> metaMenus.add(menu.toMetaMenu()));
        this.loginToken(eruptUser.toMetaUser(), metaMenus, token, tokenExpire);
    }

    public void loginToken(EruptUser eruptUser, String token) {
        this.loginToken(eruptUser, token, eruptUpmsProp.getExpireTimeByLogin());
    }

    public void logoutToken(String name, String token) {
        if (!eruptProp.isRedisSession()) request.getSession().invalidate();
        for (String uk : SessionKey.USER_KEY_GROUP) eruptSessionService.remove(uk + token);
        log.info("logout erupt-token: {} → {}", name, token);
    }

}
