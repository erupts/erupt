package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface Search {
    boolean value();

    //vague支持类型：INPUT CHOICE SLIDER DATE TEXTAREA
    boolean vague() default false;
}
