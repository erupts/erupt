package xyz.erupt.security.service;

import org.springframework.stereotype.Service;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;

/**
 * @author liyuepeng
 * @date 2020/12/5 14:57
 */
@Service
public class EruptSecurityService {

    @Resource
    private EruptSessionService sessionService;

    @Resource
    private EruptUserService eruptUserService;

    public boolean verifyToken(String token) {
        return null != sessionService.get(SessionKey.USER_TOKEN + token);
    }

    public boolean verifyMenuAuth(String name) {
        return name.equalsIgnoreCase(eruptUserService.getCurrentMenu().getValue());
    }

    public boolean verifyEruptMenuAuth(String authStr, EruptModel eruptModel) {
        //校验authStr与请求头erupt信息是否匹配，来验证其合法性
        if (!authStr.equalsIgnoreCase(eruptModel.getEruptName())) {
            return false;
        }
        //检验注解
        if (!eruptModel.getErupt().authVerify()) {
            return true;
        }
        //校验菜单权限
        return verifyMenuAuth(eruptModel.getEruptName());
    }

}
