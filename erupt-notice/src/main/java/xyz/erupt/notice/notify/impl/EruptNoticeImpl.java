package xyz.erupt.notice.notify.impl;

import org.springframework.stereotype.Component;
import xyz.erupt.notice.notify.NoticeHandler;

@Component
public class EruptNoticeImpl extends NoticeHandler {
    @Override
    public String name() {
        return "";
    }

    @Override
    public void send(String title, String content) {

    }
}
