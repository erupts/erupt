package xyz.erupt.ai.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface AiFuncParam {

    String value();

    boolean required() default true;

}
