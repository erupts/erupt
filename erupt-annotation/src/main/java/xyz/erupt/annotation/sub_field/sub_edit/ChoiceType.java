package xyz.erupt.annotation.sub_field.sub_edit;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.ChoiceTrigger;

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

    @Deprecated
    @Transient
    @Comment("Accessible by the trigger interface")
    String[] triggerParams() default {};

    @Deprecated
    @Comment("Action triggered when a selection is made")
    @Match("#item.trigger().getSimpleName() != 'ChoiceTrigger'")
    Class<? extends ChoiceTrigger> trigger() default ChoiceTrigger.class;

    //Linkage capability
    @Comment("Linkage capability; dependent field name")
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String dependField() default "";

    enum Type {
        @Comment("Dropdown selection")
        SELECT,
        @Comment("Radio button")
        RADIO,
    }
}