package xyz.erupt.mybatis.annotation;

import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2021/3/12 16:39
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface BindXmlMapper {

    @Comment("xml file path")
    String value();
}
