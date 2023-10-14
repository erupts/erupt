package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.constant.AnnotationConst;

public @interface CheckboxType {

    @Comment("存储列")
    String id() default AnnotationConst.ID;

    @Comment("展示列")
    String label() default AnnotationConst.LABEL;

    @Comment("描述列")
    String remark() default AnnotationConst.EMPTY_STR;

}
