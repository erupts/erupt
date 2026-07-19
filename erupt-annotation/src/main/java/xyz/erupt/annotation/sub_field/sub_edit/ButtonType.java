package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.EruptButtonHandler;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2026-07-19
 */
public @interface ButtonType {

    @Comment("Button style: default, primary, dashed, link, text")
    String style() default "default";

    @Comment("Danger button style")
    boolean danger() default false;

    @Comment("Button icon, support fontawesome icons, e.g. fa fa-check")
    String icon() default "";

    @Comment("Confirmation hint before executing; empty means no confirmation")
    String confirm() default "";

    @Comment("Display the whole line")
    boolean fullSpan() default false;

    @Transient
    @Comment("Click handler, receives all current form values")
    Class<? extends EruptButtonHandler> handler() default EruptButtonHandler.class;

    @Transient
    @Comment("Annotation callback parameters passed to the handler")
    String[] handlerParams() default {};

}
