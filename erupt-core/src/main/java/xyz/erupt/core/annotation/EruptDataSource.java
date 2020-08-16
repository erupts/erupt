package xyz.erupt.core.annotation;


import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2020-01-13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface EruptDataSource {

    String value();
}
