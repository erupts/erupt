package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 2019-01-13.
 */
public @interface DependSwitchType {
    DependSwitchAttr[] dependSwitchAttrs();

    boolean reject() default true;
}
