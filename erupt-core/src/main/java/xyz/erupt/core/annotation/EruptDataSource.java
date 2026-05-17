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
@Comment("Multiple data source annotation")
public @interface EruptDataSource {

    @Comment("Multiple data source name: erupt.dbs[i].datasource.name")
    String value();
}
