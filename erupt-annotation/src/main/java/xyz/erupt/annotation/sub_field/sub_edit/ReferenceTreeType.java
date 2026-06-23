package xyz.erupt.annotation.sub_field.sub_edit;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.expr.Expr;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public @interface ReferenceTreeType {

    @Comment("Storage column")
    String id() default AnnotationConst.ID;

    @Comment("Display column")
    String label() default AnnotationConst.LABEL;

    @Comment("Parent node identifier column")
    String pid() default "";

    @Transient
    @Comment("Identifies what characteristic of pid marks a root node; must be used together with filter")
    Expr rootPid() default @Expr;

    @Comment("Dependent field")
    @Language(value = "java", prefix = "Object get() { ", suffix = ";}")
    String dependField() default "";

    @Transient
    @Comment("The column name associated with the dependent field value; dependField.value = this.dependColumn")
    String dependColumn() default AnnotationConst.ID;

    @Comment("Number of expanded levels")
    int expandLevel() default 999;
}
