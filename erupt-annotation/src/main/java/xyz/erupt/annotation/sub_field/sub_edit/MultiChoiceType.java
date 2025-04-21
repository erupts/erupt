package xyz.erupt.annotation.sub_field.sub_edit;

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
    @Comment("手动配置选择项")
    VL[] vl() default {};

    @Transient
    @Comment("可被fetchHandler接口获取到")
    String[] fetchHandlerParams() default {};

    @Transient
    @Comment("动态获取选择项")
    Class<? extends ChoiceFetchHandler>[] fetchHandler() default {};

    @Comment("联动时依赖字段名")
    String dependField() default "";

    enum Type {
        @Comment("下拉多选")
        SELECT,
        @Comment("多选框")
        CHECKBOX,
    }
}