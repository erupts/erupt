package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.TagsFetchHandler;

import java.beans.Transient;

public @interface TagsType {

    @Comment("Separator character when storing multiple tags")
    String joinSeparator() default "|";

    @Comment("Whether custom tags are allowed")
    boolean allowExtension() default true;

    @Comment("Maximum number of tags")
    int maxTagCount() default 9999;

    @Transient
    @Comment("List of selectable tags")
    String[] tags() default {};

    @Transient
    @Comment("This configuration is accessible from fetchHandler")
    String[] fetchHandlerParams() default {};

    @Comment("Dynamically fetched list of selectable tags")
    Class<? extends TagsFetchHandler>[] fetchHandler() default {};
}
