package xyz.erupt.core.annotation;


import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2020-01-13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Comment("多数据源注解")
public @interface EruptDataSource {

    @Comment("应该为：erupt.dbs[i].datasource.name")
    String value();
}
