package xyz.erupt.notice.notify;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public abstract class NoticeHandler {

    @Getter
    private static final Map<String, NoticeHandler> handlers = new HashMap<>();

    public NoticeHandler() {
        handlers.put(this.getClass().getSimpleName(), this);
    }

    public abstract String name();

    public abstract String send(String title, String content);

}
