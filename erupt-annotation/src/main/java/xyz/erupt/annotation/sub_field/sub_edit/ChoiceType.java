package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2018-10-09.
 */
public @interface ChoiceType {

    String joinSeparator() default "|";

    ChoiceEnum type() default ChoiceEnum.SELECT_SINGLE;

    VL[] vl() default {};

//    Class<? extends VLEnum> vlEnum() default VLEnum.class;

    @Transient
    String[] fetchHandlerParams() default {};

    @Transient
    Class<? extends ChoiceFetchHandler>[] fetchHandler() default {};
}