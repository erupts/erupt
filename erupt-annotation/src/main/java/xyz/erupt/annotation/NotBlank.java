package xyz.erupt.annotation;

/**
 * Created by liyuepeng on 2019-05-27.
 */
public @interface NotBlank {
    boolean value() default true;
}
