package xyz.erupt.annotation;

/**
 * @author YuePeng
 * date 2019-05-22.
 */
public @interface KV {
    String key();

    String value();

    String desc() default "";
}
