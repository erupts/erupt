package com.erupt.annotation.sub_field.sub_edit;

import com.erupt.annotation.constant.UiColor;

/**
 * Created by liyuepeng on 10/9/18.
 */
public @interface ChoiceType {
    VL[] vl();

    ChoiceEnum type() default ChoiceEnum.SINGLE;

    UiColor color() default UiColor.primary;
}