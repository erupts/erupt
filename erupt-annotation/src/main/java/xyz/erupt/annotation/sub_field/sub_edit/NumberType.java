package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author liyuepeng
 * @date 2019-09-30.
 */
public @interface NumberType {
    long max() default Long.MAX_VALUE;

    int min() default 0;
}
