package xyz.erupt.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author YuePeng
 * date 2025/4/20 14:44
 */
@Target({ElementType.FIELD})
public @interface Ref {
    Class<?> value();
}
