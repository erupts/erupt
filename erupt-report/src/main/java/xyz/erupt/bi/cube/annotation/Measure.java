package xyz.erupt.bi.cube.annotation;

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

    String sql();

    String filter() default "";

    boolean hidden() default false;

    boolean canFilter() default true;

    String[] drillFields() default {};

    String[] tags() default {};

}
