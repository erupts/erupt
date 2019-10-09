package xyz.erupt.annotation.sub_field;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import xyz.erupt.annotation.config.Empty;
import xyz.erupt.annotation.config.JavaTypeEnum;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;

import java.lang.annotation.*;

/**
 * Created by liyuepeng on 2019-05-24.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface EditTypeMapping {

    Class<? extends Annotation> mapping() default Empty.class;

    String desc() default "";

    boolean search() default true;

    boolean searchVague() default true;

    JavaTypeEnum[] allowType() default {};

    boolean excelOperator() default true;

    //TODO 重要的组件依据
    Class<?> component() default void.class;
}
