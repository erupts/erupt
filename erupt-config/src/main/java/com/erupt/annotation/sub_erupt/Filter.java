package com.erupt.annotation.sub_erupt;

import com.erupt.annotation.fun.ConditionHandler;

/**
 * Created by liyuepeng on 11/5/18.
 */
public @interface Filter {
    String condition() default "";

    Class<? extends ConditionHandler>[] conditionHandlers() default {};
}
