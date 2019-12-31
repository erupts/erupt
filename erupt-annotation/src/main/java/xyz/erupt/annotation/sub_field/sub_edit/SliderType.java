package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author liyuepeng
 * @date 2019-01-10.
 */
public @interface SliderType {
    int max();

    int min() default 0;

    int step() default 1;

    boolean dots() default false;

    int[] markPoints() default {};
}
