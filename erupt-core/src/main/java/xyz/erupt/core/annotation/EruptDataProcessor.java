package xyz.erupt.core.annotation;

import xyz.erupt.core.service.IEruptDataService;
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
    Class<? extends IEruptDataService> value() default EruptDbService.class;
}
