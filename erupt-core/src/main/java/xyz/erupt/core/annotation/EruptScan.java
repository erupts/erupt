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
@Comment("Erupt项目包扫描核心注解")
public @interface EruptScan {

    @Comment("需要被扫描的包名")
    String[] value() default {};

}
