package xyz.erupt.annotation.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//使用Volatile修饰的注解字段不要随意修改属性，以为会被Volatile动态执行的方法覆盖掉
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Volatile {

    Class<? extends VolatileFun> value();
}
