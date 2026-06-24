package xyz.erupt.annotation.cube;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2025/11/2 20:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Measure {

    String title();

    String description() default "";

    FieldType type() default FieldType.AUTO;

    @Language(value = "sql", prefix = "select ")
    String sql();

    @Language("markdown")
    String prompt() default "";

    @Language(value = "java", prefix = "Object get() { ", suffix = ";}")
    String[] drillFields() default {};

    @Language(value = "sql", prefix = "select * from x where ")
    String drillFilter() default "";

    boolean hidden() default false;

    String[] tags() default {};

}
