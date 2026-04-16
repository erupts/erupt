package xyz.erupt.annotation.sub_erupt;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.expr.ExprBool;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2020-02-17.
 */
public @interface Drill {

    String code() default "";

    String title();

    @Comment("Whether to display as collapsed")
    boolean fold() default false;

    @Comment("Refer to Font Awesome for icon names")
    @Language(value = "html", prefix = "<i class=\"", suffix = "\"></i>")
    String icon() default "fa fa-sitemap";

    @Comment("Drill-down target configuration")
    Link link();

    @Transient
    ExprBool show() default @ExprBool;

}
