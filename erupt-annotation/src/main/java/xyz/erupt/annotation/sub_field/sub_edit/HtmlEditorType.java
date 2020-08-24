package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author liyuepeng
 * @date 2020-02-15
 */
public @interface HtmlEditorType {

    Type value();

    enum Type {
        CKEDITOR, UEDITOR
    }
}
