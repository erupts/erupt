package xyz.erupt.annotation.sub_field.sub_edit;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2018-10-10.
 */
public @interface InputType {
    int length() default 255;

    String type() default "text";

    boolean fullSpan() default false;

    @Transient
    String regex() default "";

    VL[] prefix() default {};

    VL[] suffix() default {};
}
