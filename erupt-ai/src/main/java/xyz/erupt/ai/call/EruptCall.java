package xyz.erupt.ai.call;

import org.springframework.stereotype.Component;
import xyz.erupt.core.service.EruptCoreService;

/**
 * @author YuePeng
 * date 2025/3/14 23:25
 */
@Component
public class EruptCall implements AiFunction {

    @Override
    public String name() {
        return "有多少个erupt类";
    }

    @Override
    public String call(String param) {
        return EruptCoreService.getErupts().size() + "";
    }
}
