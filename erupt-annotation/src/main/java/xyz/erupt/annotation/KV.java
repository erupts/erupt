package xyz.erupt.annotation;

import java.lang.annotation.Annotation;

/**
 * @author liyuepeng
 * @date 2019-05-22.
 */
public @interface KV {
    String key();

    String value();

    String desc() default "";

    Class<? extends Annotation>[] annotation() default {};
}
