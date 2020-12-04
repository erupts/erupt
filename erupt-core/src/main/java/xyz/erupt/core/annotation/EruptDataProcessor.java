package xyz.erupt.core.annotation;

import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2019-04-28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EruptDataProcessor {

    String value();

}
