package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.fun.ConditionHandler;

import java.beans.Transient;

/**
 * Created by liyuepeng on 11/5/18.
 */
public @interface Filter {
    @Transient
    String condition();

    Class<? extends ConditionHandler>[] conditionHandlers() default {};
}
