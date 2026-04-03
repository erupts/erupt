package xyz.erupt.annotation.cube;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.fun.CubeProxy;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2025/11/2 20:52
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Cube {

    @Language(value = "java", prefix = "private String ", suffix = ";")
    String datasource() default "";

    String name();

    String description() default "";

    String sql();

    SqlType sqlType() default SqlType.SUB_QUERY;

    // Define the rules for external exposure
    Explore[] explores() default {};

    String[] tags() default {};

    Class<? extends CubeProxy>[] dataProxy() default {};

}