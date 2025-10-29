package xyz.erupt.annotation.view;

public @interface GanttView {

    String startDateField();

    String endDateField();

    String pidField() default "";

}
