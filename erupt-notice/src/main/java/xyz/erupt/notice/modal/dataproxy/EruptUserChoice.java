package xyz.erupt.notice.modal.dataproxy;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.channel.NoticeChannelHandler;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

import java.util.List;

@Component
public class EruptUserChoice implements ChoiceFetchHandler {

    @Resource
    private EruptDao eruptDao;

    @Override
    public List<VLModel> fetch(String[] params) {
        return eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getStatus, true).list().stream().map(noticeHandler
                -> new VLModel(noticeHandler.getId(), noticeHandler.getName())).toList();
    }

}
