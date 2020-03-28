package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author liyuepeng
 * @date 2018-09-28.
 */
public @interface Search {
    boolean value() default true;

    boolean notNull() default false;

    //vague支持类型：INPUT CHOICE SLIDER DATE TEXTAREA
    boolean vague() default false;
}
