package xyz.erupt.monitor.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.EruptProp;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.monitor.constant.MonitorConstant;
import xyz.erupt.monitor.vo.Platform;
import xyz.erupt.monitor.vo.Server;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2021/1/23 21:36
 */
@RestController
@RequestMapping(MonitorConstant.REST_MONITOR + "/server.html")
public class ServerController {

    @Resource
    private EruptProp eruptProp;

    @RequestMapping("/info")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU)
    public Server info() {
        return new Server();
    }

    @RequestMapping("/gc")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU)
    public void gc() {
        System.gc();
    }

    @RequestMapping("/platform")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU)
    public Platform getPlatformInfo() {
        Platform platform = new Platform();
        AttachmentProxy attachmentProxy = EruptUtil.findAttachmentProxy();
        if (null == attachmentProxy) {
            platform.setUploadPath(eruptProp.getUploadPath());
        } else {
            platform.setUploadPath(attachmentProxy.fileDomain());
        }
        platform.setRedisSession(eruptProp.isRedisSession());
        return platform;
    }


}
