package xyz.erupt.monitor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptInformation;
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
public class MonitorServerController {

    @Resource
    private EruptProp eruptProp;

    @GetMapping("/info")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU)
    public Server info() {
        return new Server();
    }

    @GetMapping("/platform")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU)
    public Platform getPlatformInfo() {
        Platform platform = new Platform();
        AttachmentProxy attachmentProxy = EruptUtil.findAttachmentProxy();
        platform.setUploadPath(null == attachmentProxy ? eruptProp.getUploadPath() : attachmentProxy.fileDomain());
        platform.setSessionStrategy(eruptProp.isRedisSession() ? "redis" : "servlet");
        platform.setEruptCount(EruptCoreService.getErupts().size());
        platform.setEruptModules(EruptCoreService.getModules());
        platform.setEruptVersion(EruptInformation.getEruptVersion());
        return platform;
    }


}
