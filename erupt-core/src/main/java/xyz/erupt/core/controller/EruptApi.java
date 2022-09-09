package xyz.erupt.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.util.EruptInformation;
import xyz.erupt.core.util.MD5Util;

/**
 * @author YuePeng
 * date 2020-04-22
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API)
@RequiredArgsConstructor
public class EruptApi {

    @GetMapping("/version")
    public String version() {
        return EruptInformation.getEruptVersion();
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
