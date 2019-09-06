package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.constant.AnnotationConst;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface ReferenceTableType {
    String id() default AnnotationConst.ID;

    String label() default AnnotationConst.LABEL;

    boolean add() default true;

    boolean delete() default true;

    boolean modify() default true;
}
