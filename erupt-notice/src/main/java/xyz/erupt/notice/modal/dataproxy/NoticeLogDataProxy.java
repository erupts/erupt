package xyz.erupt.notice.modal.dataproxy;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.modal.NoticeLog;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.notice.service.EruptNoticeService;
import xyz.erupt.upms.model.EruptUser;

@Component
public class NoticeLogDataProxy implements DataProxy<NoticeLog> {

    @Resource
    private EruptNoticeService eruptNoticeService;

    @Resource
    private EruptDao eruptDao;

    @Override
    public void afterAdd(NoticeLog noticeLog) {
        NoticeMessage noticeMessage = new NoticeMessage();
        EruptUser eruptUser = eruptDao.find(EruptUser.class, noticeLog.getReceiveUser().getId());
        eruptNoticeService.send(noticeLog.getChannels().stream().toList(), eruptUser, noticeMessage);
    }

}
