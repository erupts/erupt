package com.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 10/22/18.
 */
public @interface NumberType {
    int max();

    int min() default 0;

    int defaultVal() default 0;
}
