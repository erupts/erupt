package xyz.erupt.ai.call.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.call.AiFunctionCall;
import xyz.erupt.core.module.EruptModuleInvoke;

/**
 * @author YuePeng
 * date 2025/3/14 23:25
 */
@Component
@Scope("prototype")
public class EruptModuleInfo implements AiFunctionCall {

    @Override
    public String description() {
        return "Erupt Module list";
    }

    @Override
    public String call(String prompt) {
        StringBuilder sb = new StringBuilder();
        EruptModuleInvoke.invoke(it -> {
            sb.append(it.info().getName()).append(": ").append(it.info().getDescription()).append("\n");
        });
        return sb.toString();
    }
}
