package xyz.erupt.bi.cube.annotation;

import org.intellij.lang.annotations.Language;
import xyz.erupt.bi.cube.constant.DimensionType;

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

    @Language(value = "sql",suffix = "select ")
    String sql() default "";

    String description() default "";

    boolean primaryKey() default false;

    boolean canFilter() default true;

    DimensionType dataType() default DimensionType.AUTO;

    // 格式化表达式，可使用 js 语法，内置变量 value 表示当前值
    @Language("javascript")
    String format() default "";

    boolean hidden() default false;

    String[] tags() default {};

}
