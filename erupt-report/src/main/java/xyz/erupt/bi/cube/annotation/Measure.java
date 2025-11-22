package xyz.erupt.bi.cube.annotation;

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

    @Language(value = "sql",prefix = "select ")
    String sql();

    // 格式化表达式，可使用 js 语法，内置变量 value 表示当前值
    @Language("javascript")
    String format() default "";

    String filter() default "";

    boolean hidden() default false;

    boolean canFilter() default true;

    String[] drillFields() default {};

    String[] tags() default {};

}
