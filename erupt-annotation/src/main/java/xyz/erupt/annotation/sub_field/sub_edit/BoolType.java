package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.constant.AnnotationValue;

/**
 * @author YuePeng
 * date 2018-09-18.
 */
public @interface BoolType {

    String trueText() default AnnotationValue.Y;

    String falseText() default AnnotationValue.N;
}
