package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2019-01-10.
 */
public @interface SliderType {

    int min() default 0;

    int max();

    @Comment("Step size")
    float step() default 1;

    @Comment("Scale mark points")
    float[] markPoints() default {};

    @Comment("Whether the slider can only be dragged to mark points")
    boolean dots() default false;
}
