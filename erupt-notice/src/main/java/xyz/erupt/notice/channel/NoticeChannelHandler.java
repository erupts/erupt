package xyz.erupt.notice.channel;

import lombok.Getter;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.upms.model.EruptUser;

import java.util.HashMap;
import java.util.Map;

public abstract class NoticeChannelHandler {

    @Getter
    private static final Map<String, NoticeChannelHandler> handlers = new HashMap<>();

    public NoticeChannelHandler() {
        handlers.put(this.code(), this);
    }

    public String code() {
        return this.getClass().getSimpleName();
    }

    public abstract String name();

    public abstract void send(EruptUser receiveUser, NoticeMessage noticeMessage);

}
