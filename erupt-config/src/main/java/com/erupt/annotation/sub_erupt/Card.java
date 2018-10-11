package com.erupt.annotation.sub_erupt;

import com.erupt.annotation.ColorEnum;

/**
 * Created by liyuepeng on 10/11/18.
 */
public @interface Card {
    String icon();

    String value();

    String txt();

    ColorEnum color() default ColorEnum.DEFAULT;
}
