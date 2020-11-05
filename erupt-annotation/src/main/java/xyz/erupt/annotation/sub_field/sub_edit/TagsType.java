package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.fun.TagsFetchHandler;

import java.beans.Transient;

public @interface TagsType {

    String joinSeparator() default "|";

    boolean allowExtension() default true;

    @Transient
    String[] tags() default {};

    @Transient
    String[] fetchHandlerParams() default {};

    @Transient
    Class<? extends TagsFetchHandler>[] fetchHandler() default {};
}
