package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.FilterHandler;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-11-05.
 */
public @interface Filter {

    @Transient
    @Comment("Data filter expression")
    String value() default "";

    @Transient
    @Comment("Can be retrieved by conditionHandler")
    String[] params() default {};

    @Transient
    @Comment("Dynamically process filter conditions")
    Class<? extends FilterHandler> conditionHandler() default FilterHandler.class;
}
