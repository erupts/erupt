package xyz.erupt.annotation.config;

import java.lang.annotation.*;

@Comment("Do not arbitrarily modify attributes of annotation fields decorated with @Volatile; they will be overwritten by the method dynamically executed by Volatile")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Volatile {

    Class<? extends VolatileFun<? extends Annotation, ? extends Annotation>> value();

}
