package xyz.erupt.annotation.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use SpEL expressions to verify the annotation information to determine whether to serialize, and the expression must return a boolean type.
 *
 * @author YuePeng
 * date 2020-02-21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Match {

    /**
     * Pre-injected variable value：
     * value : The current annotation variable
     * item :  Parent annotation variable
     *
     * @return expression
     */
    String value();
}
