package xyz.erupt.core.annotation;

import xyz.erupt.core.service.DataService;
import xyz.erupt.core.service.data_impl.DBService;

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
    Class<? extends DataService> processors() default DBService.class;
}
