package xyz.erupt.monitor.model;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.config.EruptProp;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.service.EruptSessionService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/2/16 14:34
 */
@Service
public class LogOutOperationHandler implements OperationHandler<EruptOnline, Void> {

    @Resource
    private EruptProp eruptProp;

    @Resource
    private EruptSessionService eruptSessionService;

    @Override
    public void exec(List<EruptOnline> data, Void v, String[] param) {
        if (eruptProp.isRedisSession()) {
            for (EruptOnline datum : data) {
                eruptSessionService.remove(SessionKey.USER_TOKEN + datum.getToken());
            }
        }
    }

}
