package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.FilterHandler;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2018-11-05.
 */
public @interface Filter {

    @Transient
    @Comment("数据过滤表达式")
    String value() default "";

    @Transient
    @Comment("可被conditionHandler获取")
    String[] params() default {};

    @Transient
    @Comment("数据动态过滤接口")
    Class<? extends FilterHandler> conditionHandler() default FilterHandler.class;
}
