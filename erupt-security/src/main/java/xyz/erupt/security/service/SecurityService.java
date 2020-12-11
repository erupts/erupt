package xyz.erupt.security.service;

import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.service.EruptSessionService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2020/12/5 14:57
 */
@Service
public class SecurityService {

    @Resource
    private EruptSessionService sessionService;

    public boolean verifyToken(String token) {
        return null != sessionService.get(SessionKey.USER_TOKEN + token);
    }

    public boolean verifyMenuAuth(String token, String name) {
        List<EruptMenu> menus = sessionService.get(SessionKey.MENU + token, new TypeToken<List<EruptMenu>>() {
        }.getType());
        for (EruptMenu menu : menus) {
            if (name.equalsIgnoreCase(menu.getValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean verifyEruptMenuAuth(String token, String authStr, EruptModel eruptModel) {
        //校验authStr与请求头erupt信息是否匹配，来验证其合法性
        if (!authStr.equalsIgnoreCase(eruptModel.getEruptName())) {
            return false;
        }
        //检验注解
        if (!eruptModel.getErupt().authVerify()) {
            return true;
        }
        //校验菜单权限
        return verifyMenuAuth(token, eruptModel.getEruptName());
    }

}
