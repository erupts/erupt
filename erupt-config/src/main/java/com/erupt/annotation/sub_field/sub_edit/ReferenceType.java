package com.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface ReferenceType {
    String id();

    String label();

    String filter() default "";
}
