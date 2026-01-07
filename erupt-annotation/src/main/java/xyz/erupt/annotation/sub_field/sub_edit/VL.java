package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2018-10-11.
 */
public @interface VL {

    String value();

    String label();

    @Comment("hex format")
    String color() default "";

    boolean disable() default false;

    String desc() default "";

    String extra() default "";
}
