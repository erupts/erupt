package xyz.erupt.upms.annotation;

import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Comment("菜单权限校验")
public @interface EruptMenuAuth {

    String value();

}
