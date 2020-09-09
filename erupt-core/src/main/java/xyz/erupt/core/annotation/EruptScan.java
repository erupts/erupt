package xyz.erupt.core.annotation;

import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2020-09-09
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface EruptScan {
    String[] value() default {};
}
