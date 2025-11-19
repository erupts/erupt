package xyz.erupt.bi.cube.annotation;

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

    String title();;

    String description() default "";

    String sql();

    boolean explore() default true;

    String[] tags() default {};

}
