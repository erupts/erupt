package xyz.erupt.notice.channel;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.webscoket.channel.SocketCommand;
import xyz.erupt.webscoket.model.EruptWsSessionModel;
import xyz.erupt.webscoket.service.EruptWebSocketService;

@Component
public class EruptInternalNotice extends AbstractNoticeChannel {

    @Resource
    private EruptWebSocketService webSocketService;

    @Override
    public Integer order() {
        return -1;
    }

    @Override
    public String name() {
        return I18nTranslate.$translate("notice.channel.internal");
    }

    @Override
    public void send(EruptUser eruptUser, NoticeMessage noticeMessage) {
        for (EruptWsSessionModel model : webSocketService.getAllSession()) {
            if (model.getMetaUserinfo().getId().equals(eruptUser.getId())) {
                webSocketService.send(model, SocketCommand.JS,
                        "window.eruptNotice(" + noticeMessage.getId() + ",`" + noticeMessage.getTitle() + "`, `" + noticeMessage.getContent() + "`)");
            }
        }
    }

}
