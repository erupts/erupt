package xyz.erupt.core.annotation;

import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2020-05-26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface EruptApi {

    boolean value() default true;

    String desc();
}
