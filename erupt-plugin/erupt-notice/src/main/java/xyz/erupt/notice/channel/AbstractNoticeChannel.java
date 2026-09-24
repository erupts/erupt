package xyz.erupt.notice.channel;

import lombok.Getter;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.upms.model.EruptUser;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractNoticeChannel {

    @Getter
    private static final Map<String, AbstractNoticeChannel> handlers = new HashMap<>();

    public AbstractNoticeChannel() {
        handlers.put(this.code(), this);
    }

    public Integer order() {
        return 0;
    }

    public String code() {
        return this.getClass().getSimpleName();
    }

    public abstract String name();

    /**
     * Whether the channel can be picked right now. A channel that depends on configuration
     * made at runtime, such as a provider row, hides itself until that configuration exists.
     */
    public boolean available() {
        return true;
    }

    public abstract void send(EruptUser receiveUser, NoticeMessage noticeMessage);

}
