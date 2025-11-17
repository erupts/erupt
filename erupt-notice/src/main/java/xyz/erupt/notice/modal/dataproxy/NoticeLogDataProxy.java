package xyz.erupt.notice.modal.dataproxy;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.notice.modal.NoticeLog;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.notice.service.EruptNoticeService;

@Component
public class NoticeLogDataProxy implements DataProxy<NoticeLog> {

    @Resource
    private EruptNoticeService eruptNoticeService;

    @Override
    public void afterAdd(NoticeLog noticeLog) {
        NoticeMessage noticeMessage = new NoticeMessage();
        noticeMessage.setTitle(noticeLog.getTitle());
        noticeMessage.setContent(noticeLog.getContent());
        eruptNoticeService.send(noticeLog.getChannels().stream().toList(), noticeLog.getReceiveUsers().stream().toList(), noticeMessage);
    }

}
