package xyz.erupt.upms.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.upms.service.EruptUserService;

/**
 * @author YuePeng
 * date 2022/3/9 19:18
 */
@RestController
@AllArgsConstructor
public class EruptUPMSController {

    private final EruptUserService eruptUserService;

    //用户信息
    @GetMapping(EruptRestPath.USERINFO)
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public MetaUserinfo userinfo() {
        MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfo();
        metaUserinfo.setId(null);
        return metaUserinfo;
    }

    //校验菜单类型值权限
    @GetMapping(EruptRestPath.ERUPT_CODE_PERMISSION + "/{value}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public boolean eruptPermission(@PathVariable("value") String menuValue) {
        return null != eruptUserService.getEruptMenuByValue(menuValue);
    }

}
