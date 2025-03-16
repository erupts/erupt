package xyz.erupt.ai.call;

import org.springframework.stereotype.Component;
import xyz.erupt.core.context.MetaContext;

/**
 * @author YuePeng
 * date 2025/3/14 23:25
 */
@Component
public class UserCall implements AiFunction {

    @Override
    public String name() {
        return "当前系统用户";
    }

    @Override
    public String call(String param) {
        return MetaContext.getUser().getName();
    }
}
