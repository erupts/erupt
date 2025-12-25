package xyz.erupt.ai.call.impl;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiParam;
import xyz.erupt.ai.call.AiFunctionCall;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;

/**
 * @author YuePeng
 * date 2025/3/14 23:25
 */
@Component
@Scope("prototype")
public class EruptData implements AiFunctionCall {

    @AiParam(description = "Erupt Name")
    private String eruptName;

    @Resource
    private EruptDao eruptDao;

    @Override
    public String description() {
        return "Erupt model data";
    }

    @Override
    public String call(String prompt) {
        EruptModel erupt = EruptCoreService.getErupt(eruptName);
        List<?> result = eruptDao.lambdaQuery(erupt.getClazz()).limit(50).list();
        return GsonFactory.getGson().toJson(result);
    }
}
