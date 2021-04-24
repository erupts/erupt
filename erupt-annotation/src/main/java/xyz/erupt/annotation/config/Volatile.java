package xyz.erupt.annotation.config;

import java.lang.annotation.*;

@Comment("使用Volatile修饰的注解字段不要随意修改属性，会被Volatile动态执行的方法覆盖掉")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Volatile {

    Class<? extends VolatileFun<? extends Annotation, ? extends Annotation>> value();

}
