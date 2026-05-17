package xyz.erupt.core.annotation;

import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2019-04-28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EruptDataProcessor {

    @Comment("1. Implement the xyz.erupt.core.service.IEruptDataService interface")
    @Comment("2. Register the implementation class via: DataProcessorManager.register('dataSourceName', EruptDataServiceXxx.class);")
    @Comment("3. The value is the name of an already registered 'data source'")
    String value();

}
