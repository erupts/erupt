package xyz.erupt.upms.annotation;

import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2023/6/9 22:40
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Comment("Login authentication")
public @interface EruptLoginAuth {

}
