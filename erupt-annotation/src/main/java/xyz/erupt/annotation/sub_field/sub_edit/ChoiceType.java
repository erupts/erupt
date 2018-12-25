package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 10/9/18.
 */
public @interface ChoiceType {
    VL[] vl();

    ChoiceEnum type() default ChoiceEnum.SELECT_SINGLE;
}