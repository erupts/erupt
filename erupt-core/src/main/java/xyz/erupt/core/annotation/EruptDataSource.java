package xyz.erupt.core.annotation;


import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2020-01-13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Comment("多数据源注解")
public @interface EruptDataSource {

    @Comment("多数据源名称：erupt.dbs[i].datasource.name")
    String value();
}
