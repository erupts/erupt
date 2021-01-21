package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.AutoCompleteHandler;

import java.beans.Transient;

public @interface AutoCompleteType {

    @Transient
    @Comment("动态生成自动完成列表")
    Class<? extends AutoCompleteHandler> handler();

    @Transient
    @Comment("可在handler中获取到")
    String[] param() default {};

    @Comment("触发字符最小长度")
    int triggerLength() default 1;
}
