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
    public String description() {
        return "询问当前系统登录用户";
    }

    @Override
    public String call(String prompt) {
        return "Current user: " + MetaContext.getUser().getName();
    }
}
