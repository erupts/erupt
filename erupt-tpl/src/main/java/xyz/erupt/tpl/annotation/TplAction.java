package xyz.erupt.tpl.annotation;

import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
@Comment("修饰方法必须返回Map<String,Object>类型")
public @interface TplAction {

    @Comment("tpl目录下文件名称，该值也作为权限特征使用")
    String value();

    @Comment("文件路径，为空则以 value 值作为文件路径")
    String path() default "";

}
