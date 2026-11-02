package xyz.erupt.annotation.vis;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

/**
 * Options of a {@code Vis.Type.MAP} view: every row becomes a marker. The
 * position comes from a {@code EditType.MAP} field (its JSON carries lng/lat)
 * or from a pair of numeric fields. Clicking a marker opens the row.
 *
 * @author YuePeng
 */
public @interface MapView {

    @Comment("EditType.MAP field holding the position; empty uses lngField / latField")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String locationField() default "";

    @Language(value = "hql", prefix = "select ", suffix = " from")
    String lngField() default "";

    @Language(value = "hql", prefix = "select ", suffix = " from")
    String latField() default "";

    @Comment("Hex color field for the marker")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String colorField() default "";

    @Comment("Merge nearby markers into clusters while zoomed out")
    boolean cluster() default true;

}
