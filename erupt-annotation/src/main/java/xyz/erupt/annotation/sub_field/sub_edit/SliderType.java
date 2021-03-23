package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2019-01-10.
 */
public @interface SliderType {
    int max();

    int min() default 0;

    @Comment("步进长度")
    int step() default 1;

    @Comment("刻度标记")
    int[] markPoints() default {};

    @Comment("是否只能拖拽到刻度上")
    boolean dots() default false;
}
