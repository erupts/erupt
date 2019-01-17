package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 2019-01-17.
 */
public @interface ImageType {
    int width() default 0;

    int height() default 0;

    String[] types() default {};

    //单位KB
    long size() default 0;

    boolean muti() default false;

    //标记宽高是否需要完全匹配
    boolean vagueWidth() default false;

    boolean vagueHeight() default false;
}
