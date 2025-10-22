package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2020-05-21
 */
public @interface DynamicBy {

    @Comment("依赖字段名")
    String dependField();

    @Comment("显示条件表达式，支持变量：value 该值表示依赖字段的值")
    String expr();

    @Comment("展示类型")
    Type viewType() default Type.HIDE;

    Type passType() default Type.SHOW;

    enum Type {
        SHOW,     //显示
        HIDE,     //隐藏
        NOTNULL,  //必填
        READONLY, //只读
    }

}
