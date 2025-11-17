package xyz.erupt.notice.notify.impl;

import org.springframework.stereotype.Component;
import xyz.erupt.notice.notify.NoticeHandler;
import xyz.erupt.upms.model.EruptUser;

@Component
public class EruptNoticeImpl extends NoticeHandler {
    @Override
    public String name() {
        return "";
    }

    @Override
    public void send(EruptUser eruptUser, String title, String content) {

    }
}
