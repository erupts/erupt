package xyz.erupt.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface BaseDataProxy {
    Class<? extends xyz.erupt.annotation.fun.DataProxy> value();
}
