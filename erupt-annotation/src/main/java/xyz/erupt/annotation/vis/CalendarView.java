package xyz.erupt.annotation.vis;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2026/6/2
 */
public @interface CalendarView {

    @Language(value = "hql", prefix = "select ", suffix = " from")
    String dateField();

    @Comment("Optional end date field for multi-day events")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String endDateField() default "";

    @Comment("Hex color field for event styling")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String colorField() default "";

}
