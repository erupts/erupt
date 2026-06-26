package xyz.erupt.annotation.cube;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.CubeProxy;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2025/11/2 20:52
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EruptCube {

    @Comment("Datasource key, blank uses the primary datasource")
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String datasource() default "";

    String name();

    String description() default "";

    @Comment("Source SQL, interpreted as a sub-query or table name per sqlType")
    @Language("VTL")
    String sql();

    @Comment("How sql is interpreted: sub-query or table name")
    SqlType sqlType() default SqlType.SUB_QUERY;

    // Define the rules for external exposure
    Explore[] explores() default {
            @Explore(
                    code = "overview",
                    name = "Overview"
            )
    };

    @Comment("AI prompt")
    @Language("markdown")
    String prompt() default "";

    String[] tags() default {};

    Class<? extends CubeProxy>[] dataProxy() default {};

}