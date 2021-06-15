package xyz.erupt.core.annotation;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2021/6/15 15:06
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface EruptHandlerNaming {

    String value();

}
