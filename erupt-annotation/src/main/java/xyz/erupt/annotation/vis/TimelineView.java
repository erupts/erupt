package xyz.erupt.annotation.vis;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

/**
 * Options of a {@code Vis.Type.TIMELINE} view: a vertical feed of the rows
 * ordered by {@link #dateField()}, one entry per row. The first visible column
 * is the entry title, the remaining visible columns its detail lines.
 *
 * @author YuePeng
 */
public @interface TimelineView {

    @Language(value = "hql", prefix = "select ", suffix = " from")
    String dateField();

    @Comment("Hex color field for the entry dot")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String colorField() default "";

    @Comment("Newest entries first")
    boolean descending() default true;

    @Comment("Entries on the right of the axis, alternating, or on the left")
    Mode mode() default Mode.LEFT;

    enum Mode {
        LEFT, ALTERNATE, RIGHT
    }

}
