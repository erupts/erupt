package com.erupt.annotation.sub_field.sub_edit;

import com.erupt.annotation.ColorEnum;
import com.erupt.annotation.constant.DataLength;

/**
 * Created by liyuepeng on 10/10/18.
 */
public @interface InputType {
    InputEnum inputEnum() default InputEnum.STRING;

    ColorEnum color() default ColorEnum.DEFAULT;

    int length() default DataLength.UNLIMITED;

    String placeholder() default "";

    String defaultVal() default "";

    String icon() default "";
}
