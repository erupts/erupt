package xyz.erupt.notice.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.channel.NoticeChannelHandler;
import xyz.erupt.notice.modal.NoticeLog;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Date;
import java.util.List;

@Service
public class EruptNoticeService {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptUserService eruptUserService;

    public void send(List<String> channels, EruptUser receiveUser, NoticeMessage noticeMessage) {
        NoticeLog noticeLog = new NoticeLog();
        noticeLog.setTitle(noticeMessage.getTitle());
        noticeLog.setContent(noticeMessage.getContent());
        noticeLog.setReceiveUser(new EruptUserVo(receiveUser.getId()));
        noticeLog.setCreateTime(new Date());
        noticeLog.setCreateUser(new EruptUserVo(eruptUserService.getCurrentUid()));
        eruptDao.persist(noticeLog);
        for (String channel : channels) {
            NoticeChannelHandler.getHandlers().get(channel).send(receiveUser, noticeMessage);
        }
    }

}
