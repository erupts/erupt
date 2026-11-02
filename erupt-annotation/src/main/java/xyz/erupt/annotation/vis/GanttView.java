package xyz.erupt.annotation.vis;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface GanttView {

    @Language(value = "hql", prefix = "select ", suffix = " from")
    String startDateField();

    @Language(value = "hql", prefix = "select ", suffix = " from")
    String endDateField();

    @Language(value = "hql", prefix = "select ", suffix = " from")
    String groupField() default "";

    @Language(value = "hql", prefix = "select * from t where ")
    String pidField() default "";

    // max value is 100
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String progressField() default "";

    // hex2rgb
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String colorField() default "";

    @Comment("Field holding the predecessors of a row: a @ManyToOne reference or a @ManyToMany collection " +
            "of this same model. Rendered as finish-to-start links; dragging a link on the chart writes it back")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String dependencyField() default "";

    @Comment("Boolean field: a row holding true is drawn as a milestone diamond on its start date instead of a bar")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String milestoneField() default "";

}
