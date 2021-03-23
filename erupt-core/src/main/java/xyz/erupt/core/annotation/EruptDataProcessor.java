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

    @Comment("1、实现 xyz.erupt.core.service.IEruptDataService 接口")
    @Comment("2、将实现类通过：DataProcessorManager.register('数据源名称', EruptDataServiceXxx.class); 方法注册")
    @Comment("3、value值为已注册 '数据源' 名称")
    String value();

}
