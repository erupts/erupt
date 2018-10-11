package com.erupt.annotation.sub_erupt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Power {
    boolean add() default true;

    boolean del() default true;

    boolean edit() default true;

    boolean query() default true;

    boolean export() default true;
}
