package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.fun.FilterHandler;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2018-11-05.
 */
public @interface Filter {
    @Transient
    String condition();

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends FilterHandler>[] conditionHandlers() default {};
}
