package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.Empty;
import xyz.erupt.annotation.config.JavaTypeEnum;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2019-05-24.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface EditTypeMapping {

    Class<? extends Annotation> mapping() default Empty.class;

    String desc() default "";

    JavaTypeEnum[] allowType() default {};

    boolean excelOperator() default true;

    String[] nameInfer() default {};
}
