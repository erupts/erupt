package xyz.erupt.notice.channel.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.notice.channel.NoticeChannelHandler;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.webscoket.model.EruptWsSessionModel;
import xyz.erupt.webscoket.service.EruptWebSocketService;

@Component
public class EruptNoticeImpl extends NoticeChannelHandler {

    @Resource
    private EruptWebSocketService webSocketService;

    @Override
    public String name() {
        return "站内通知";
    }

    @Override
    public void send(EruptUser eruptUser, NoticeMessage noticeMessage) {
        for (EruptWsSessionModel model : webSocketService.getAllSession()) {
            if (model.getMetaUserinfo().getId().equals(eruptUser.getId())) {
                webSocketService.sendJsNotify(noticeMessage.getTitle(), noticeMessage.getContent());
            }
        }
    }

}
