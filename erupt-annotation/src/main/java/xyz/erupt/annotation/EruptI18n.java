package xyz.erupt.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EruptI18n {
    boolean enable() default true;
}
