package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.JavaType;

import java.lang.annotation.*;

/**
 * Created by liyuepeng on 2019-05-24.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface EditTypeMapping {

    String mapping() default "";

    String desc() default "";

    boolean search() default true;

    boolean searchVague() default true;

    JavaType[] allowType() default {};

    boolean excelOperator() default true;

    //TODO 重要的组件依据
    Class<?> component() default void.class;
}
