package xyz.erupt.annotation.vis;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface BoardView {

    @Language(value = "hql", prefix = "select ", suffix = " from")
    String groupField();

    @Comment("Second grouping: one horizontal lane per value, cards move between lanes as well")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String swimlaneField() default "";

    @Comment("Numeric field summed in every column header next to the card count")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String sumField() default "";

    @Comment("Work-in-progress limit per column; a column holding more cards is highlighted, 0 disables")
    int wipLimit() default 0;

}
