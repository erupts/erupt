package xyz.erupt.print.var.impl;

import org.springframework.stereotype.Component;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.print.var.PrintVar;

/**
 * @author YuePeng
 * date 2026/1/21 20:56
 */
@Component
public class CurrentUser extends PrintVar {

    @Override
    public String name() {
        return "Current User";
    }

    @Override
    public Object value() {
        return MetaContext.getUser().getUid();
    }

}
