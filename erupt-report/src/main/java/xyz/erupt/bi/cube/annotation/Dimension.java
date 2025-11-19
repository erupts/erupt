package xyz.erupt.bi.cube.annotation;

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

    String description() default "";

    boolean primaryKey() default false;

    String column();

    boolean canFilter() default true;

    DimensionType dataType() default DimensionType.AUTO;

    String format() default "";

    boolean hidden() default false;

    String[] tags() default {};

}
