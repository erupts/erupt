package xyz.erupt.upms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2022/3/9 19:18
 */
@RestController
public class EruptUPMSController {

    @Resource
    private EruptUserService eruptUserService;

    //用户信息
    @GetMapping(EruptRestPath.USERINFO)
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public MetaUserinfo userinfo() {
        MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfo();
        metaUserinfo.setId(null);
        return metaUserinfo;
    }

    //校验菜单类型值权限
    @RequestMapping(EruptRestPath.ERUPT_CODE_PERMISSION)
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public boolean eruptPermission(String menuValue) {
        return null != eruptUserService.getEruptMenuByValue(menuValue);
    }

}
