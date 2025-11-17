package xyz.erupt.notice.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.channel.NoticeChannelHandler;
import xyz.erupt.notice.modal.NoticeLog;
import xyz.erupt.upms.annotation.EruptLoginAuth;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/notice")
public class EruptNoticeController {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptUserService eruptUserService;

    @EruptLoginAuth
    @GetMapping("/channels")
    public List<VLModel> list() {
        return NoticeChannelHandler.getHandlers().values().stream().map(h
                -> new VLModel(h.code(), h.name())).toList();
    }

    @EruptLoginAuth
    @GetMapping("/messages")
    public List<NoticeLog> messages() {
        return eruptDao.lambdaQuery(NoticeLog.class).with(NoticeLog::getReceiveUser)
                .eq(EruptUserVo::getId, eruptUserService.getCurrentUid()).list();
    }

}
