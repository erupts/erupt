package xyz.erupt.annotation.cube;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

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

    @Comment("Aggregate SQL expression, e.g. sum(amount)")
    @Language(value = "sql", prefix = "select ")
    String sql();

    @Comment("AI prompt")
    @Language("markdown")
    String prompt() default "";

    @Comment("Dimension fields exposed when drilling down")
    @Language(value = "java", prefix = "Object get() { ", suffix = ";}")
    String[] drillFields() default {};

    @Comment("Extra filter applied when drilling down")
    @Language(value = "sql", prefix = "select * from x where ")
    String drillFilter() default "";

    boolean hidden() default false;

    String[] tags() default {};

}
