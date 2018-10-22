package com.erupt.annotation.sub_field.sub_edit;

import com.erupt.annotation.constant.UiColor;
import com.erupt.annotation.constant.DataLength;

/**
 * Created by liyuepeng on 10/10/18.
 */
public @interface InputType {
    InputEnum type() default InputEnum.TEXT;

    UiColor color() default UiColor.DEFAULT;

    int length() default DataLength.NAME_LENGTH;

    String placeholder() default "";

    String defaultVal() default "";

    String icon() default "";
}
