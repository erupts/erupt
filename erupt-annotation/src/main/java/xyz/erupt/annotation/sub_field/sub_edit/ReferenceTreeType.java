package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.constant.AnnotationConst;

import java.beans.Transient;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface ReferenceTreeType {
    String id() default AnnotationConst.ID;

    String label() default AnnotationConst.LABEL;

    String pid() default "";

    String dependField() default "";

    @Transient
    String dependColumn() default AnnotationConst.ID;
}
