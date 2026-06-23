package xyz.erupt.annotation.sub_field.sub_edit;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2025-01-19.
 */
public @interface MultiChoiceType {

    Type type() default Type.CHECKBOX;

    @Transient
    @Comment("Manually configured options")
    VL[] vl() default {};

    @Transient
    @Comment("Accessible by the fetchHandler interface")
    String[] fetchHandlerParams() default {};

    @Transient
    @Comment("Dynamically fetched options")
    Class<? extends ChoiceFetchHandler>[] fetchHandler() default {};

    @Comment("Dependent field name for linkage")
    @Language(value = "java", prefix = "Object get() { ", suffix = ";}")
    String dependField() default "";

    enum Type {
        @Comment("Dropdown multi-select")
        SELECT,
        @Comment("Checkbox group")
        CHECKBOX,
    }
}