package xyz.erupt.annotation.sub_field.sub_edit;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2020-02-15
 */
public @interface HtmlEditorType {

    @Comment("Rich text editor type")
    Type value();

    @Transient
    @Comment("Define a dedicated storage path for easier file lookup")
    @Language("file-reference")
    String path() default "";

    enum Type {
        CKEDITOR, UEDITOR
    }
}
