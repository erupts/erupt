package xyz.erupt.monitor.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptInformation;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.monitor.constant.MonitorConstant;
import xyz.erupt.monitor.service.CpuSampleService;
import xyz.erupt.monitor.service.IoSampleService;
import xyz.erupt.monitor.vo.Platform;
import xyz.erupt.monitor.vo.Server;
import xyz.erupt.upms.annotation.EruptMenuAuth;

/**
 * @author YuePeng
 * date 2021/1/23 21:36
 */
@RestController
@RequestMapping(MonitorConstant.REST_MONITOR + "/server")
public class MonitorServerController {

    @Resource
    private EruptProp eruptProp;

    @Resource
    private IoSampleService ioSampleService;

    @Resource
    private CpuSampleService cpuSampleService;

    @GetMapping("/info")
    @EruptMenuAuth(MonitorConstant.MENU_SERVER)
    public Server info() {
        Server server = new Server();
        server.setCpu(cpuSampleService.sample());
        server.setIo(ioSampleService.sample());
        return server;
    }

    @GetMapping("/platform")
    @EruptMenuAuth(MonitorConstant.MENU_SERVER)
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
