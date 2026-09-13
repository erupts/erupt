package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.AutoCompleteHandler;

import java.beans.Transient;

public @interface AutoCompleteType {

    @Transient
    @Comment("Predefined candidates, matched case-insensitively against the input; combined with handler results")
    String[] values() default {};

    @Transient
    @Comment("Dynamically generates the autocomplete list; the interface itself means no handler")
    Class<? extends AutoCompleteHandler> handler() default AutoCompleteHandler.class;

    @Transient
    @Comment("Accessible from the handler")
    String[] param() default {};

    @Comment("Minimum input length to trigger autocomplete")
    int triggerLength() default 1;
}
