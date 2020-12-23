package xyz.erupt.annotation.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author liyuepeng
 * @date 2020-12-23.
 */

@Target({ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface Comments {

    Comment[] value();

}