package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2020-02-15
 */
public @interface HtmlEditorType {

    @Comment("富文本编辑器类型")
    Type value();

    @Comment("定义独享存储空间，便于文件查找")
    String path() default "";

    enum Type {
        CKEDITOR, UEDITOR
    }
}
