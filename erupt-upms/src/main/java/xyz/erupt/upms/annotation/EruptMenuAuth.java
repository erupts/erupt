package xyz.erupt.upms.annotation;

import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Comment("Menu permission validation")
public @interface EruptMenuAuth {

    String value();

}
