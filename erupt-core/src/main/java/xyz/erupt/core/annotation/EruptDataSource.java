package xyz.erupt.core.annotation;


import org.springframework.orm.jpa.vendor.Database;

import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2020-01-13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface EruptDataSource {
    String sourceName();

    Database database();

//    String beanPrefix() default "entity_manager_";
}
