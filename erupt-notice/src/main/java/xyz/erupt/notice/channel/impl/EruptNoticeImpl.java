package xyz.erupt.notice.channel.impl;

import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.stereotype.Component;
import xyz.erupt.notice.channel.NoticeChannelHandler;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.webscoket.service.EruptWebSocketService;

@Component
public class EruptNoticeImpl extends NoticeChannelHandler {

    @Resource
    private EruptWebSocketService webSocketService;

    @Override
    public String name() {
        return "Erupt Notify";
    }

    @Override
    public void send(EruptUser eruptUser, NoticeMessage noticeMessage) {
        webSocketService.sendJsNotify(noticeMessage.getTitle(), noticeMessage.getContent());
    }

}
