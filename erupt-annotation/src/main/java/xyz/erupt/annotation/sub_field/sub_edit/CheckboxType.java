package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.constant.AnnotationConst;

public @interface CheckboxType {

    @Comment("Storage column")
    String id() default AnnotationConst.ID;

    @Comment("Display column")
    String label() default AnnotationConst.LABEL;

    @Comment("Description column")
    String remark() default AnnotationConst.EMPTY_STR;

}
