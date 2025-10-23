package xyz.erupt.annotation.sub_field.sub_edit;

import lombok.Getter;
import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2025-10-22
 */
public @interface DependOn {

    @Comment("依赖字段名")
    String dependField();

    @Comment("条件依赖表达式，变量：value 依赖字段的值")
    String condition();

    Ctrl noMatch() default Ctrl.HIDE;

    Ctrl match() default Ctrl.SHOW;

    enum Ctrl {
        SHOW,     //显示
        HIDE,     //隐藏
        NOTNULL,  //必填
        READONLY, //只读
    }

    @Getter
    class Var {

        private String value;

    }

}
