package xyz.erupt.notice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.notice.notify.NoticeHandler;

import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/notice")
public class EruptNoticeController {

    @GetMapping("/list")
    public List<VLModel> list() {
        return NoticeHandler.getHandlers().values().stream().map(h
                -> new VLModel(h.code(), h.name())).toList();
    }

}
