package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 2019-09-30.
 */
public @interface NumberType {
    long max() default Long.MAX_VALUE;

    int min() default 0;
}
