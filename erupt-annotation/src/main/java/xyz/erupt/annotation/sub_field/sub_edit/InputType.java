package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-10-10.
 */
public @interface InputType {

    @Comment("Maximum input length")
    int length() default 255;

    String type() default "text";

    @Comment("Display the whole line")
    boolean fullSpan() default false;

    @Transient
    @Comment("Regex the input value")
    String regex() default "";

    @Transient
    @Comment("Automatically trim input value")
    boolean autoTrim() default true;

    VL[] prefix() default {};

    VL[] suffix() default {};
}
