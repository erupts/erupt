package xyz.erupt.notice.notify;

import java.util.HashMap;
import java.util.Map;

public abstract class NoticeHandler {

    private static final Map<String, NoticeHandler> handlers = new HashMap<>();

    public NoticeHandler() {
        handlers.put(this.name(), this);
    }

    public abstract String name();

    public abstract String send(String title, String content);

}
