package com.erupt.annotation.sub_field;

import com.erupt.annotation.sub_field.sub_edit.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Edit {

    String title() default "";

    boolean notNull() default false;

    EditType type() default EditType.INPUT;

    boolean show() default true;

    boolean readOnly() default false;

    int sort() default 0;

    String group() default "";


    //如下注解虽为数组形式但是实际使用中只取数组为第零个的值
    InputType[] inputType() default {};

    ReferenceType[] referenceType() default {};

    BoolType[] boolType() default {};

    ChoiceType[] choiceType() default {};

    DictType[] dictType() default {};

}
