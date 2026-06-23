package xyz.erupt.annotation.sub_field.sub_edit;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.constant.AnnotationConst;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public @interface ReferenceTableType {

    @Comment("Storage column")
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String id() default AnnotationConst.ID;

    @Comment("Display column")
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String label() default AnnotationConst.LABEL;

    @Comment("Dependent field")
    @Language(value = "java", prefix = "Object get() { ", suffix = ";}")
    String dependField() default "";

    @Transient
    @Comment("The column name associated with the dependent field value; dependField.value = this.dependColumn")
    @Language(value = "java", prefix = "private String ", suffix = ";")
    String dependColumn() default AnnotationConst.ID;
}
