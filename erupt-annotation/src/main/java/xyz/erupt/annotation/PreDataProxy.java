package xyz.erupt.annotation;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.DataProxy;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface PreDataProxy {

    Class<? extends DataProxy<?>> value();

    @Comment("This value can be retrieved via DataProxyContext.get() inside dataProxy")
    String[] params() default {};

}
