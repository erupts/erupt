package xyz.erupt.notice.service;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.channel.AbstractNoticeChannel;
import xyz.erupt.notice.channel.EruptInternalNotice;
import xyz.erupt.notice.constant.NoticeStatus;
import xyz.erupt.notice.modal.NoticeLog;
import xyz.erupt.notice.modal.NoticeLogDetail;
import xyz.erupt.notice.modal.NoticeScene;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class EruptNoticeService {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptUserService eruptUserService;

    @Transactional
    public void send(AbstractNoticeChannel abstractNoticeChannel, String scene, List<Long> receiveUsers, NoticeMessage noticeMessage) {
        NoticeScene noticeScene = eruptDao.lambdaQuery(NoticeScene.class).eq(NoticeScene::getCode, scene).one();
        this.send(noticeScene, List.of(abstractNoticeChannel.code()), receiveUsers, noticeMessage);
    }

    @Transactional
    public void send(NoticeScene noticeScene, List<String> channels, List<Long> receiveUsers, NoticeMessage noticeMessage) {
        NoticeLog noticeLog = new NoticeLog();
        noticeLog.setNoticeScene(noticeScene);
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
                if (Objects.equals(channel, EruptInternalNotice.class.getSimpleName())) {
                    noticeLogDetail.setStatus(NoticeStatus.UNREAD);
                } else {
                    noticeLogDetail.setStatus(NoticeStatus.SENT);
                }
                AbstractNoticeChannel noticeChannel = AbstractNoticeChannel.getHandlers().get(channel);
                try {
                    noticeChannel.send(eruptDao.find(EruptUser.class, userId), noticeMessage);
                } catch (Exception e) {
                    noticeLogDetail.setSuccess(false);
                    noticeLogDetail.setError(e.toString());
                    log.error("{} send error: {}", noticeChannel.name(), e.getMessage());
                }
                eruptDao.persist(noticeLogDetail);
            }
        }
    }

}
