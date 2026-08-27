package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2019-09-30.
 */
public @interface NumberType {

    long max() default Integer.MAX_VALUE;

    long min() default -Integer.MAX_VALUE;

    @Comment("Decimal precision. -1 keeps the value as typed, 0 forces integer, N keeps N decimals")
    int precision() default -1;

    @Comment("Step size for the increment / decrement controls and mouse wheel")
    double step() default 1;

    @Comment("Prefix unit shown inside the input, e.g. ¥ / $")
    String prefix() default "";

    @Comment("Suffix unit shown inside the input, e.g. % / kg / ms")
    String suffix() default "";

    @Comment("Show a thousands separator for readability (display only, does not affect the stored value)")
    boolean thousandsSeparator() default false;
}
