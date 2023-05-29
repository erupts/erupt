package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.SceneEnum;
import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2020-05-21
 */
@Deprecated
public @interface DynamicBy {

    boolean enable() default true;

//    @Comment("依赖其他表单的值")
//    String[] dependFields();

    @Comment("显示条件表达式，支持变量：item 该值表示其他字段的值")
    String expr();

    @Comment("展示类型")
    Type viewType() default Type.SHOW;

    @Comment("生效场景范围")
    SceneEnum[] sceneRange() default {SceneEnum.ADD, SceneEnum.EDIT, SceneEnum.VIEW_DETAIL};


    enum Type {
        SHOW, //显示
        HIDE, //隐藏
        SHOW_NOTNULL, //显示且必填
        SHOW_READONLY, //显示且制度
    }

}
