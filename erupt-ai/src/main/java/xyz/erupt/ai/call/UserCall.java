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
        return "当前用户";
    }

    @Override
    public String call(String param) {
        return "当前登录用户：" + MetaContext.getUser().getName();
    }
}
