package xyz.erupt.annotation;

import java.lang.annotation.Annotation;

/**
 * Created by liyuepeng on 2019-05-22.
 */
public @interface KV {
    String key();

    String value();

    String desc() default "";

    Class<? extends Annotation>[] annotation() default {};
}
