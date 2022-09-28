package xyz.erupt.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.util.EruptInformation;

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

}
