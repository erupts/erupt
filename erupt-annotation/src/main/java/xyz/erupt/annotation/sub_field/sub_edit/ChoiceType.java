package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public @interface ChoiceType {

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

    @Comment("开启后在编辑等操作时会重新获取下拉列表")
    boolean anewFetch() default false;

    //联动能力
    @Comment("联动能力，依赖字段名")
    String dependField() default "";

    @Comment("联动过滤表达式，支持变量：")
    @Comment("dependValue:依赖字段的值")
    @Comment("vl.value: 下拉列表项的值")
    @Comment("vl.label: 下拉列表项中文名称")
    String dependExpr() default "false";

    enum Type {
        @Comment("下拉选择")
        SELECT,
        @Comment("单选框")
        RADIO,
    }
}