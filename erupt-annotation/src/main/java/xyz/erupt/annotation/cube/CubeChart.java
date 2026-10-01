package xyz.erupt.annotation.cube;

import xyz.erupt.annotation.KV;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.ToMap;

/**
 * One chart of a {@link CubeView}. {@link #x()} names dimensions, {@link #y()}
 * measures, {@link #series()} the dimension that splits the data into series;
 * for a pivot table they are rows, values and columns, for a heat map x and
 * series are the two axes and y the color. A bare field name refers to the view's
 * own cube; a joined cube's field is written {@code OtherCube.field}.
 *
 * @author YuePeng
 */
public @interface CubeChart {

    String title() default "";

    Type type();

    @Comment("Dimension fields: x axis, category, pivot rows")
    String[] x() default {};

    @Comment("Measure fields: y axis, value, pivot values")
    String[] y() default {};

    @Comment("Series dimension: legend, pivot columns, heat map y axis")
    String series() default "";

    @Comment("Grid columns this chart spans")
    int span() default 1;

    @Comment("Extra chart options, e.g. color, fontSize for KPI")
    @ToMap(key = "key")
    KV[] ui() default {};

    /**
     * Mirrors the report types of erupt-cube's puzzle dashboard.
     */
    enum Type {
        LINE, AREA, BAR, COLUMN, PIE, SCATTER, RADAR, FUNNEL, DUAL_AXES, GAUGE, WATERFALL, WORD_CLOUD, ROSE, RADIAL_BAR,
        SANKEY, CHORD, MAP, BUBBLE, TREEMAP, HEATMAP,
        TINY_LINE, TINY_AREA, TINY_COLUMN, PROGRESS, RING_PROGRESS, TABLE, PIVOT_TABLE, KPI
    }

}
