package xyz.erupt.notice.service;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.channel.NoticeChannelHandler;
import xyz.erupt.notice.modal.NoticeLog;
import xyz.erupt.notice.modal.NoticeLogDetail;
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

    @Transactional
    public void send(List<String> channels, List<Long> receiveUsers, NoticeMessage noticeMessage) {
        NoticeLog noticeLog = new NoticeLog();
        noticeLog.setTitle(noticeMessage.getTitle());
        noticeLog.setContent(noticeMessage.getContent());
        noticeLog.setCreateTime(new Date());
        noticeLog.setCreateUser(new EruptUserVo(eruptUserService.getCurrentUid()));
        eruptDao.persist(noticeLog);
        for (String channel : channels) {
            for (Long userId : receiveUsers) {
                NoticeLogDetail noticeLogDetail = new NoticeLogDetail();
                noticeLogDetail.setChannel(channel);
                noticeLogDetail.setNoticeLog(noticeLog);
                noticeLogDetail.setReceiveUser(new EruptUserVo(userId));
                noticeLogDetail.setSuccess(true);
                try {
                    NoticeChannelHandler.getHandlers().get(channel).send(eruptDao.find(EruptUser.class, userId), noticeMessage);
                } catch (Exception e) {
                    noticeLogDetail.setSuccess(false);
                    noticeLogDetail.setError(e.getMessage());
                }
                eruptDao.persist(noticeLogDetail);
            }
        }
    }

}
