package xyz.erupt.annotation;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.cube.CubeView;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Sort;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.vis.BoardView;
import xyz.erupt.annotation.vis.CalendarView;
import xyz.erupt.annotation.vis.CardView;
import xyz.erupt.annotation.vis.GanttView;
import xyz.erupt.annotation.vis.MapView;
import xyz.erupt.annotation.vis.TableView;
import xyz.erupt.annotation.vis.TimelineView;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface Vis {

    String code() default "";

    String title();

    String desc() default "";

    FieldVisibility fieldVisibility() default FieldVisibility.EXCLUDE;

    @Comment("Fields to display or exclude")
    @Language(value = "hql", prefix = "select ", suffix = " from ")
    String[] fields() default {};

    @Transient
    Filter[] filter() default {};

    @Transient
    Sort[] orderBy() default {};

    @Transient
    ExprBool show() default @ExprBool;

    Type type() default Type.TABLE;

    @Match("#item.type().toString() == 'BOARD'")
    BoardView boardView() default @BoardView(groupField = "");

    @Match("#item.type().toString() == 'CARD'")
    CardView cardView() default @CardView();

    @Match("#item.type().toString() == 'GANTT'")
    GanttView ganttView() default @GanttView(startDateField = "", endDateField = "");

    @Match("#item.type().toString() == 'CALENDAR'")
    CalendarView calendarView() default @CalendarView(dateField = "");

    @Match("#item.type().toString() == 'TPL'")
    Tpl tplView() default @Tpl(enable = false, path = "");

    @Match("#item.type().toString() == 'TABLE'")
    TableView tableView() default @TableView;

    @Match("#item.type().toString() == 'TIMELINE'")
    TimelineView timelineView() default @TimelineView(dateField = "");

    @Match("#item.type().toString() == 'MAP'")
    MapView mapView() default @MapView;

    @Match("#item.type().toString() == 'CUBE'")
    CubeView cubeView() default @CubeView(charts = {});

    enum Type {
        TABLE,
        GANTT,
        CARD,
        BOARD,
        CALENDAR,
        // vertical feed ordered by a date field
        TIMELINE,
        // markers on a map from a MAP field or a lng/lat pair
        MAP,
        // charts over an @EruptCube explore, filtered by the current search (rendered by erupt-cube)
        CUBE,
        TPL
    }

    enum FieldVisibility {
        // Include fields in the view
        INCLUDE,
        // Exclude fields in the view
        EXCLUDE
    }

}
