package xyz.erupt.tpl.annotation;

import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2020-02-24
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EruptTpl {

    Tpl engine() default Tpl.Thymeleaf;

    enum Tpl {
        FreeMarker,
        Thymeleaf
    }
}
