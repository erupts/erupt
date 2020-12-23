package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2018-10-09.
 */
public @interface ChoiceType {

    @Comment("UI样式")
    Type type() default Type.SELECT;

    @Transient
    @Comment("手动配置选择项")
    VL[] vl() default {};

    @Transient
    @Comment("可被fetchHandler接口获取到")
    String[] fetchHandlerParams() default {};

    @Transient
    @Comment("动态获取选择项")
    Class<? extends ChoiceFetchHandler>[] fetchHandler() default {};

    enum Type {
        @Comment("下拉选择")
        SELECT,
        @Comment("单选框")
        RADIO,
    }
}