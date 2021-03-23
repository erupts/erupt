package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author YuePeng
 * date 2019-09-30.
 */
public @interface NumberType {
    long max() default Integer.MAX_VALUE;

    long min() default -Integer.MAX_VALUE;
}
