package xyz.erupt.annotation;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.viz.CardView;
import xyz.erupt.annotation.viz.GanttView;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface Viz {

    String code() default "";

    String title();

    String desc() default "";

    @Comment("Specify the view fields. If no configuration is made, all fields will be displayed.")
    String[] fields() default {};

    @Transient
    Filter[] filter() default {};

    @Transient
    String orderBy() default "";

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
