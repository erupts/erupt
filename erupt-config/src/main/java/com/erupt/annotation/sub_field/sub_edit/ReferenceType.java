package com.erupt.annotation.sub_field.sub_edit;

import java.lang.annotation.*;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ReferenceType {
    String id() default "";

    String label() default "";
}
