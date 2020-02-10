package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.constant.DataLength;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2018-10-10.
 */
public @interface InputType {
    int length() default DataLength.TEXT_LENGTH;

    @Transient
    String regex() default "";

    VL[] prefix() default {};

    VL[] suffix() default {};
}
