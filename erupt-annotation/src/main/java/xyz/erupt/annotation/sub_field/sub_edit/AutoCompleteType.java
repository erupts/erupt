package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.fun.AutoCompleteHandler;

import java.beans.Transient;

public @interface AutoCompleteType {

    @Transient
    Class<? extends AutoCompleteHandler> handler();

    @Transient
    String[] param() default {};

    int triggerLength() default 1;
}
