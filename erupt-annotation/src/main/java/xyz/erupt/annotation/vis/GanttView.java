package xyz.erupt.annotation.vis;

import org.intellij.lang.annotations.Language;

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

}
