package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.constant.DataLength;

/**
 * Created by liyuepeng on 10/10/18.
 */
public @interface InputType {
    int length() default DataLength.LONG_TEXT_LENGTH;

    VL[] prefix() default {};

    VL[] suffix() default {};
}
