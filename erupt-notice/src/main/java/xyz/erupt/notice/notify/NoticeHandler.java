package xyz.erupt.notice.notify;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public abstract class NoticeHandler {

    @Getter
    private static final Map<String, NoticeHandler> handlers = new HashMap<>();

    public NoticeHandler() {
        handlers.put(this.code(), this);
    }

    public String code() {
        return this.getClass().getSimpleName();
    }

    public abstract String name();

    public abstract void send(String title, String content);

}
