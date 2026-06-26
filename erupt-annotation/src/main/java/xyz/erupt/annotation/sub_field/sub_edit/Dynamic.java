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
    @Language(value = "java", prefix = "Object get() { ", suffix = ";}")
    String dependField();

    @Comment("JS expression，Variable: class field name")
    @Language("javascript")
    String condition();

    Ctrl noMatch() default Ctrl.HIDE;

    Ctrl match() default Ctrl.SHOW;

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
