package com.erupt.annotation.sub_field.sub_edit;

import com.erupt.annotation.sub_erupt.Filter;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface ReferenceType {
    String id();

    String label();

    Filter filter() default @Filter;

    String depend() default "";
}
