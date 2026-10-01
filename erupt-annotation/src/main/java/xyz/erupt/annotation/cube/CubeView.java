package xyz.erupt.annotation.cube;

import xyz.erupt.annotation.KV;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.ToMap;

/**
 * A grid of charts over an {@link EruptCube} explore, declared in code instead of a
 * designed dashboard. Used by {@code @Vis(type = Vis.Type.CUBE, cubeView = ...)} to put
 * charts next to a model's other views; with {@link #linkSearch()} the list's search
 * conditions are forwarded to every chart as cube filters. Rendering and querying need
 * the erupt-cube module; without it the view reports that charts are unavailable.
 *
 * @author YuePeng
 */
public @interface CubeView {

    @Comment("Class carrying the @EruptCube to query; Void means the annotated model, which then carries @EruptCube itself")
    Class<?> cube() default Void.class;

    String explore() default Explore.OVERVIEW;

    @Comment("Forward the current search conditions as cube filters; the searched fields must be dimensions of the cube")
    boolean linkSearch() default true;

    @Comment("Search field -> cube field when the names differ, e.g. @KV(key = \"customer.name\", value = \"customerName\"); " +
            "unmapped fields are forwarded by their own name")
    @ToMap(key = "key")
    KV[] searchMapping() default {};

    @Comment("Charts per row")
    int columns() default 2;

    @Comment("Chart height in px")
    int height() default 320;

    CubeChart[] charts();

}
