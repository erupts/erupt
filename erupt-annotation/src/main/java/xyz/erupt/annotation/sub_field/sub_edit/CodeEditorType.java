package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2020-02-15
 */
public @interface CodeEditorType {

    @Comment("语言")
    String language();

    @Comment("编辑器高度")
    int height() default 300;

}
