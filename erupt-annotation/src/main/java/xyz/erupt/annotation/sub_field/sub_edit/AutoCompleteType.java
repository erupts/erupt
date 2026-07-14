package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.AutoCompleteHandler;

import java.beans.Transient;

public @interface AutoCompleteType {

    @Transient
    @Comment("Dynamically generates the autocomplete list")
    Class<? extends AutoCompleteHandler<?>> handler();

    @Transient
    @Comment("Accessible from the handler")
    String[] param() default {};

    @Comment("Minimum input length to trigger autocomplete")
    int triggerLength() default 1;
}
