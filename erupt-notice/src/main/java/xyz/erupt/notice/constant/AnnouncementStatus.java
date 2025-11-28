package xyz.erupt.notice.constant;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.i18n.I18nTranslate;

import java.util.Arrays;
import java.util.List;

public enum AnnouncementStatus {

    OPEN,
    CLOSE;

    public static class H implements ChoiceFetchHandler {

        @Override
        public List<VLModel> fetch(String[] params) {
            return Arrays.stream(AnnouncementStatus.values()).map(noticeHandler ->
                    new VLModel(noticeHandler.name(), I18nTranslate.$translate(noticeHandler.name()))).toList();
        }

    }

}
