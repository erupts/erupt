package xyz.erupt.core.annotation;

import xyz.erupt.core.service.EruptDataService;
import xyz.erupt.core.service.data_impl.EruptDbService;

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
    Class<? extends EruptDataService> processors() default EruptDbService.class;
}
