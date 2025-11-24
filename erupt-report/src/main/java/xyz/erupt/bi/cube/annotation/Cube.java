package xyz.erupt.bi.cube.annotation;

import org.intellij.lang.annotations.Language;
import xyz.erupt.bi.cube.constant.SqlType;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2025/11/2 20:52
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Cube {

    String datasource() default "";

    String title();

    @Language("sql")
    String sql();

    SqlType sqlType() default SqlType.TABLE_NAME;

    // 定义对外暴漏规则
    Explore[] explores();

    boolean authVerify() default true;

    Parameter[] parameters() default {};

    String description() default "";

    String[] tags() default {};

}
