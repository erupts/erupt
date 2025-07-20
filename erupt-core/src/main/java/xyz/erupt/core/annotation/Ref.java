package xyz.erupt.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author YuePeng
 * date 2025/4/20 14:44
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(java.lang.annotation.RetentionPolicy.SOURCE)
public @interface Ref {
    Class<?> value();
}
