package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.TagsFetchHandler;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2026-09-02.
 */
public @interface TextareaType {

    @Comment("Maximum input length")
    int length() default Integer.MAX_VALUE;

    @Comment("Minimum visible rows")
    int minRows() default 3;

    @Comment("Maximum visible rows before scrolling")
    int maxRows() default 20;

    @Comment("Mention trigger characters, such as @ or #; empty disables mention")
    String[] mentionPrefix() default {};

    @Transient
    @Comment("Static mention suggestions")
    String[] mentions() default {};

    @Transient
    @Comment("This configuration is accessible from mentionFetchHandler")
    String[] mentionFetchHandlerParams() default {};

    @Comment("Dynamically fetched mention suggestions")
    Class<? extends TagsFetchHandler>[] mentionFetchHandler() default {};

}
