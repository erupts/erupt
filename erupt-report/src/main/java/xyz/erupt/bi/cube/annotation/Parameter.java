package xyz.erupt.bi.cube.annotation;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2025/11/2 20:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Parameter {

    boolean required() default false;

    String name();

    String description() default "";

}
