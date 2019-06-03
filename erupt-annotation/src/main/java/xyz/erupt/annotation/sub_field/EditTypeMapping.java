package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.JavaType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;

import java.lang.annotation.*;

/**
 * Created by liyuepeng on 2019-05-24.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface EditTypeMapping {
    String mapping();

    String desc() default "";

    Search search() default @Search(value = true,vague = true);

    JavaType[] allowType() default {};

    boolean excelOperator() default true;
}
