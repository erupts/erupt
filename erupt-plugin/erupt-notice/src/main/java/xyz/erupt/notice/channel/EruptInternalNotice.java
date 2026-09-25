package xyz.erupt.notice.channel;

import com.google.gson.Gson;
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

    private static final Gson GSON = new Gson();

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
        for (EruptWsSessionModel model : webSocketService.getSessionsByUser(eruptUser.getId())) {
            // title and content are user-written text: JSON-encode them so they arrive as string
            // literals instead of being spliced into the script
            // the detail row id, not the log id: the detail endpoint is keyed by the recipient's own copy
            webSocketService.send(model, SocketCommand.JS, "window.eruptNotice(" + noticeMessage.getLogDetailId() + ","
                    + GSON.toJson(noticeMessage.getTitle()) + "," + GSON.toJson(noticeMessage.getContent()) + ")");
        }
    }

}
