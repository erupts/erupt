package xyz.erupt.annotation.viz;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface GanttView {

    String startDateField();

    String endDateField();

    String groupField() default "";

    String pidField() default "";

    // max value is 100
    String progressField() default "";

    // hex2rgb
    String colorField() default "";

}
