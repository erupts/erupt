package xyz.erupt.tpl.annotation;

import xyz.erupt.annotation.sub_erupt.Tpl;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2020-02-24
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EruptTpl {

    Tpl.Engine engine() default Tpl.Engine.FreeMarker;

}
