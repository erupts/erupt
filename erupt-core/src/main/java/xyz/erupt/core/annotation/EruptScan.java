package xyz.erupt.core.annotation;

import org.springframework.context.annotation.Import;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.core.service.EruptApplication;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2020-09-09
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({EruptApplication.class})
@Comment("Core annotation for Erupt project package scanning")
public @interface EruptScan {

    @Comment("Package names to be scanned")
    String[] value() default {};

}
