package xyz.erupt.upms.model.online;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.service.EruptSessionService;

import java.util.List;

/**
 * @author YuePeng
 * date 2021/2/16 14:34
 */
@Service
public class LogOutOperationHandler implements OperationHandler<EruptOnline, Void> {

    @Resource
    private EruptSessionService eruptSessionService;

    @Override
    public String exec(List<EruptOnline> data, Void v, String[] param) {
        data.forEach(it -> {
            for (String uk : SessionKey.USER_KEY_GROUP) {
                eruptSessionService.remove(uk + it.getToken());
            }
        });
        return null;
    }

}
