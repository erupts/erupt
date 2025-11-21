package xyz.erupt.annotation.viz;

import org.intellij.lang.annotations.Language;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface GanttView {

    @Language(value = "java", prefix = "private String x", suffix = ";")
    String startDateField();

    @Language(value = "java", prefix = "private String x", suffix = ";")
    String endDateField();

    @Language(value = "java", prefix = "private String ", suffix = ";")
    String groupField() default "";

    String pidField() default "";

    // max value is 100
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String progressField() default "";

    // hex2rgb
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String colorField() default "";

}
