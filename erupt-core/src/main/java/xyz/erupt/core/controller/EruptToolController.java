package xyz.erupt.core.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.log.EruptLogManager;
import xyz.erupt.core.log.LogMessage;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_TOOL)
@RequiredArgsConstructor
public class EruptToolController {

    @GetMapping(EruptConst.ERUPT_LOG)
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU)
    public List<LogMessage> eruptLog(@RequestParam(value = "size", defaultValue = "1000") Long size,
                                     @RequestParam(value = "offset", required = false) Long offset) {
        return EruptLogManager.getEventQueue(size, offset);
    }

}
