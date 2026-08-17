package xyz.erupt.notice.model.dataproxy;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUserVo;

import java.util.List;

@Component
public class EruptUserChoice implements ChoiceFetchHandler<Void> {

    @Resource
    private EruptDao eruptDao;

    @Override
    public List<VLModel> fetch(String[] params) {
        return eruptDao.lambdaQuery(EruptUserVo.class).eq(EruptUserVo::getStatus, true).list().stream().map(user
                -> new VLModel(user.getId(), user.getName())).toList();
    }

}
