package xyz.erupt.annotation.sub_field.sub_edit;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public @interface ChoiceType {

    Type type() default Type.SELECT;

    @Transient
    @Comment("Manually configured dropdown list")
    VL[] vl() default {};

    @Transient
    @Comment("Accessible by the fetchHandler interface")
    String[] fetchHandlerParams() default {};

    @Comment("Dynamically fetched dropdown list")
    Class<? extends ChoiceFetchHandler>[] fetchHandler() default {};

    //Linkage capability
    @Comment("Linkage capability; dependent field name")
    @Language(value = "java", prefix = "Object get() { ", suffix = ";}")
    String dependField() default "";

    enum Type {
        @Comment("Dropdown selection")
        SELECT,
        @Comment("Radio button")
        RADIO,
    }
}