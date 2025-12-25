package xyz.erupt.ai.call.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.call.AiFunctionCall;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;

/**
 * @author YuePeng
 * date 2025/3/14 23:25
 */
@Component
@Scope("prototype")
public class EruptList implements AiFunctionCall {

    @Override
    public String description() {
        return "Erupt model list";
    }

    @Override
    public String call(String prompt) {
        StringBuilder sb = new StringBuilder();
        for (EruptModel erupt : EruptCoreService.getErupts()) {
            sb.append(erupt.getEruptName()).append(": ").append(erupt.getErupt().name());
        }
        return sb.toString();
    }
}
