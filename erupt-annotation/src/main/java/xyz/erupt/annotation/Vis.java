package xyz.erupt.annotation;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Sort;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.vis.CardView;
import xyz.erupt.annotation.vis.GanttView;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface Vis {

    String code() default "";

    String title();

    String desc() default "";

    @Comment("Specify the view fields. If no configuration is made, all fields will be displayed.")
    @Language(value = "hql", prefix = "select ", suffix = " from ")
    String[] fields() default {};

    @Transient
    Filter[] filter() default {};

    @Transient
    Sort[] orderBy() default {};

    @Transient
    ExprBool show() default @ExprBool;

    Type type() default Type.TABLE;

    @Match("#item.type().toString() == 'CARD'")
    CardView cardView() default @CardView();

    @Match("#item.type().toString() == 'GANTT'")
    GanttView ganttView() default @GanttView(startDateField = "", endDateField = "");

    @Match("#item.type().toString() == 'TPL'")
    Tpl tplView() default @Tpl(enable = false, path = "");

    enum Type {
        TABLE,
        GANTT,
        CARD,
        BOARD,
        TPL
    }

}
