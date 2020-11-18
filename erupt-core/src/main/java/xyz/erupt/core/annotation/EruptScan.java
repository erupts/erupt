package xyz.erupt.core.annotation;

import org.springframework.context.annotation.Import;
import xyz.erupt.core.service.EruptApplication;

import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2020-09-09
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({EruptApplication.class})
public @interface EruptScan {
    String[] value() default {};
}
