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
public @interface Dimension {

    String title();

    String description() default "";

    FieldType type() default FieldType.AUTO;

    @Comment("Group-by column expression, defaults to the field name")
    @Language("VTL")
    String sql() default "";

    @Comment("AI prompt")
    @Language("markdown")
    String prompt() default "";

    boolean hidden() default false;

    String[] tags() default {};

}
