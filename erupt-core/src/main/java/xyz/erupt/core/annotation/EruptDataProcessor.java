package xyz.erupt.core.annotation;

import xyz.erupt.core.service.data_impl.DBService;
import xyz.erupt.core.service.DataService;

import java.lang.annotation.*;

/**
 * Created by liyuepeng on 2019-04-28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EruptDataProcessor {
    Class<? extends DataService> processors() default DBService.class;
}
