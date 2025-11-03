package xyz.erupt.annotation.lens;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface GanttView {

    String startDateField();

    String endDateField();

    String pidField() default "";

}
