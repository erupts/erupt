package xyz.erupt.annotation.config;

import xyz.erupt.annotation.constant.AnnotationConst;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liyuepeng
 * @date 2019-12-20.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EruptProperty {
    String alias() default AnnotationConst.EMPTY_STR;
}
