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

    @Comment("Dynamically fetched options")
    Class<? extends ChoiceFetchHandler>[] fetchHandler() default {};

    @Comment("Dependent field name for linkage")
    @Language(value = "java", prefix = "Object get() { ", suffix = ";}")
    String dependField() default "";

    @Comment("Refuse an add or edit whose value is not one of the options (erupt.choice-validate, on by default); turn off for a field that legitimately stores values outside them")
    boolean validate() default true;

    enum Type {
        @Comment("Dropdown multi-select")
        SELECT,
        @Comment("Checkbox group")
        CHECKBOX,
        @Comment("Transfer list with search; suits large option sets")
        TRANSFER,
    }
}