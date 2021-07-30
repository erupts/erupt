package xyz.erupt.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import xyz.erupt.core.config.EruptAppProp;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.util.EruptVersionUtil;

/**
 * @author YuePeng
 * date 2020-04-22
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API)
@RequiredArgsConstructor
public class EruptApi {

    private final EruptAppProp eruptAppProp;

    //获取当前Erupt版本号
    @GetMapping("/version")
    public String version() {
        return EruptVersionUtil.getEruptVersion();
    }

    @GetMapping("/erupt-app")
    public EruptAppProp eruptApp() {
        return eruptAppProp;
    }

//    @GetMapping(value = "/erupt-app-js", produces = {"application/javascript"})
//    public String eruptAppJs() {
//        return "var eruptApp = " + GsonFactory.getGson().toJson(eruptAppProp);
//    }

    @GetMapping(value = "/erupt-machine-code")
    public String eruptMachineCode() {
        HardwareAbstractionLayer hal = new SystemInfo().getHardware();
        //CPU序列号 + 操作系统序列号 + 硬件UUID
        return hal.getProcessor().getProcessorIdentifier().getProcessorID()
                + hal.getComputerSystem().getSerialNumber()
                + hal.getComputerSystem().getHardwareUUID();
    }

}
