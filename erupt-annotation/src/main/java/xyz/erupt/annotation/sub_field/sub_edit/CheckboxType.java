package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.constant.AnnotationConst;

public @interface CheckboxType {

    String id() default AnnotationConst.ID;

    String label() default AnnotationConst.LABEL;

}
