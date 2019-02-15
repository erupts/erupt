package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.constant.DataLength;

/**
 * Created by liyuepeng on 10/10/18.
 */
public @interface InputType {
    InputEnum type() default InputEnum.TEXT;

    int length() default DataLength.NAME_LENGTH;

    String placeholder() default "";

    VL[] prefix() default {};

    VL[] suffix() default {};
}
