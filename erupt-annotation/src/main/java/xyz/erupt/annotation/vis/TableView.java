package xyz.erupt.annotation.vis;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

/**
 * Options of a {@code Vis.Type.TABLE} view. Column totals come from
 * {@code @View(statistic = ...)} and show in the table footer; with a
 * {@link #groupField()} the rows are split into collapsible groups whose
 * headers carry the count and the same per-column subtotals.
 *
 * @author YuePeng
 */
public @interface TableView {

    @Comment("Group rows by this field; empty renders the plain table")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String groupField() default "";

    @Comment("Start with every group collapsed")
    boolean collapsed() default false;

}
