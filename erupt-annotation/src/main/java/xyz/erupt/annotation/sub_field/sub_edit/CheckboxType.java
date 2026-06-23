package xyz.erupt.annotation.sub_field.sub_edit;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.constant.AnnotationConst;

public @interface CheckboxType {

    @Comment("Storage column")
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String id() default AnnotationConst.ID;

    @Comment("Display column")
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String label() default AnnotationConst.LABEL;

    @Comment("Description column")
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String remark() default AnnotationConst.EMPTY_STR;

}
