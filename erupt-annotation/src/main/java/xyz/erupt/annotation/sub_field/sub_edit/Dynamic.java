package xyz.erupt.annotation.sub_field.sub_edit;

import lombok.Getter;
import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2025-10-22
 */
public @interface Dynamic {

    @Comment("依赖字段名")
    String dependField();

    @Comment("JS expression，Variable: class field name")
    String condition();

    Ctrl noMatch() default Ctrl.HIDE;

    Ctrl match() default Ctrl.SHOW;

//    // change 当前修饰对象的静态配置，例如代码编辑器的语言
//    String changeScript() default "";

//    @Comment("Compute component value when condition changes, Variable, class field name")
//    String render() default "";

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
