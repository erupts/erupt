package xyz.erupt.annotation.sub_field.sub_edit;

import lombok.Getter;
import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2025-10-22
 */
public @interface Dynamic {

    @Comment("Dependent field name")
    String dependField();

    @Comment("JS expression，Variable: class field name")
    @Language("javascript")
    String condition();

    Ctrl noMatch() default Ctrl.HIDE;

    Ctrl match() default Ctrl.SHOW;

//    // change the static configuration of the current decorated object, e.g. the language of the code editor
//    String changeScript() default "";

//    @Comment("Compute component value when condition changes, Variable, class field name")
//    String render() default "";

    enum Ctrl {
        SHOW,     //Show
        HIDE,     //Hide
        NOTNULL,  //Required
        READONLY, //Read-only
    }

    @Getter
    class Var {

        private String value;

    }

}
