package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.constant.AnnotationConst;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2018-09-28.
 */
public @interface ReferenceTableType {
    String id() default AnnotationConst.ID;

    String label() default AnnotationConst.LABEL;

    String dependField() default "";

    @Transient
    String dependColumn() default AnnotationConst.ID;
}
