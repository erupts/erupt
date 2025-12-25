package xyz.erupt.ai.call.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiParam;
import xyz.erupt.ai.call.AiFunctionCall;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;

/**
 * @author YuePeng
 * date 2025/3/14 23:25
 */
@Component
@Scope("prototype")
public class EruptSchema implements AiFunctionCall {

    @AiParam(description = "Erupt Name")
    private String eruptName;

    @Override
    public String description() {
        return "Erupt model schema";
    }

    @Override
    public String call(String prompt) {
        EruptModel erupt = EruptCoreService.getEruptView(eruptName);
        return GsonFactory.getGson().toJson(erupt);
    }
}
