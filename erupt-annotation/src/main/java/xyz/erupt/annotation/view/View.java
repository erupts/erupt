package xyz.erupt.annotation.view;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Tpl;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface View {

    String code() default "";

    String title();

    @Comment("Specify the view fields. If no configuration is made, all fields will be displayed.")
    String[] fields() default {};

    Filter[] filter() default {};

    String orderBy() default "";

    ViewType type() default ViewType.TABLE;

    @Match("#item.viewType() == 'CARD'")
    CardView cardView() default @CardView();

    @Match("#item.viewType() == 'GANTT'")
    GanttView ganttView() default @GanttView(startDateField = "", endDateField = "");

    @Match("#item.viewType() == 'TPL'")
    Tpl tplView() default @Tpl(enable = false, path = "");

    enum ViewType {
        TABLE,
        GANTT,
        CARD,
        BOARD,
        TPL
    }

}
