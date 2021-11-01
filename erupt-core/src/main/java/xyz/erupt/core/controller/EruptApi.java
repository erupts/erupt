package xyz.erupt.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.prop.EruptAppProp;
import xyz.erupt.core.util.EruptPropUtil;
import xyz.erupt.core.util.MD5Util;

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
        return EruptPropUtil.getEruptVersion();
    }

    @GetMapping("/erupt-app")
    public EruptAppProp eruptApp() {
        return eruptAppProp;
    }

    @GetMapping(value = "/erupt-machine-code")
    public String eruptMachineCode() {
        //CPU序列号 + 操作系统序列号 + 硬件UUID
        HardwareAbstractionLayer hal = new SystemInfo().getHardware();
        return MD5Util.digest(hal.getProcessor().getProcessorIdentifier().getProcessorID()
                + hal.getComputerSystem().getSerialNumber()
                + hal.getComputerSystem().getHardwareUUID());
    }

}
