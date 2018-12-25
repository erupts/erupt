package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.constant.RgbColor;

/**
 * Created by liyuepeng on 10/11/18.
 */
public @interface Card {
    String icon();

    String value();

    String desc();

    RgbColor color() default RgbColor.blue;
}
