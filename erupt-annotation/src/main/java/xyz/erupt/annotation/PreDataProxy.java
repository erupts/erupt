package xyz.erupt.annotation;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.DataProxy;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface PreDataProxy {

    Class<? extends DataProxy<?>> value();

    @Comment("此值可在dataProxy内被DataProxyContext.get()方法中获取到")
    String[] params() default {};

}
