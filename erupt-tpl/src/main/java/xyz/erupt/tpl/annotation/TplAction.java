package xyz.erupt.tpl.annotation;

import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
@Comment("Annotated method must return Map<String,Object>")
public @interface TplAction {

    @Comment("File name under the tpl directory; also used as the permission identifier")
    String value();

    @Comment("File path; if empty, the value is used as the file path")
    String path() default "";

}
