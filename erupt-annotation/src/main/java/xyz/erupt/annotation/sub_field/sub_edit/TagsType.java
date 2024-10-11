package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.TagsFetchHandler;

import java.beans.Transient;

public @interface TagsType {

    @Comment("多个标签存储时分割字符")
    String joinSeparator() default "|";

    @Comment("是否允许自定义标签")
    boolean allowExtension() default true;

    @Comment("最大标签数")
    int maxTagCount() default 9999;

    @Transient
    @Comment("可选标签列表")
    String[] tags() default {};

    @Transient
    @Comment("该配置可从fetchHandler中获取")
    String[] fetchHandlerParams() default {};

    @Transient
    @Comment("动态获取可选标签列表")
    Class<? extends TagsFetchHandler>[] fetchHandler() default {};
}
