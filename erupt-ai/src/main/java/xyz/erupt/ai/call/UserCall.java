package xyz.erupt.ai.call;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import xyz.erupt.core.context.MetaContext;

/**
 * @author YuePeng
 * date 2025/3/14 23:25
 */
@Component
@Scope("prototype")
public class UserCall implements AiFunctionCall {

    @Override
    public String name() {
        return "Current User";
    }

    @Override
    public String description() {
        return "Ask the current system logged-in user";
    }

    @Override
    public String call(String prompt) {
        return MetaContext.getUser().getName();
    }
}
