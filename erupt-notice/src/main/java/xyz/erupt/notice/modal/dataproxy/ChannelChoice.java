package xyz.erupt.notice.modal.dataproxy;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.notice.channel.AbstractNoticeChannel;

import java.util.List;

@Component
public class ChannelChoice implements ChoiceFetchHandler {

    @Override
    public List<VLModel> fetch(String[] params) {
        return AbstractNoticeChannel.getHandlers().values().stream().map(noticeHandler
                -> new VLModel(noticeHandler.code(), noticeHandler.name())).toList();
    }

}
