package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.QueryExpression;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2020/11/27 12:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface EditTypeSearch {

    boolean value() default true;

    QueryExpression vagueMethod() default QueryExpression.EQ;
}
