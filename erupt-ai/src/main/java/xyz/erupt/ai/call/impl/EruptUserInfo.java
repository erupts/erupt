package xyz.erupt.ai.call.impl;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.call.AiFunctionCall;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUser;

/**
 * @author YuePeng
 * date 2025/3/14 23:25
 */
@Component
@Scope("prototype")
public class EruptUserInfo implements AiFunctionCall {

    @Resource
    private EruptDao eruptDao;

    @Override
    public String description() {
        return "Ask the current system logged-in user";
    }

    @Override
    public String call(String prompt) {
        return GsonFactory.getGson().toJson(eruptDao.find(EruptUser.class, MetaContext.getUser().getUid()));
    }

}
