package xyz.erupt.annotation;

import xyz.erupt.annotation.fun.DataProxy;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface PreDataProxy {

    Class<? extends DataProxy<?>> value();

}
