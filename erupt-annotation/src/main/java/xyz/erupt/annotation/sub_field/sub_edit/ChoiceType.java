package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;

import java.beans.Transient;

/**
 * Created by liyuepeng on 10/9/18.
 */
public @interface ChoiceType {

    String joinSeparator() default ",";

    ChoiceEnum type() default ChoiceEnum.SELECT_SINGLE;

    @Transient
    VL[] vl() default {};

    @Transient
    String[] fetchHandlerParams() default {};

    @Transient
    Class<? extends ChoiceFetchHandler>[] fetchHandler() default {};
}