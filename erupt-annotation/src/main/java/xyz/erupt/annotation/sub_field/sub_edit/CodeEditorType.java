package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.CodeEditHintHandler;

/**
 * @author YuePeng
 * date 2020-02-15
 */
public @interface CodeEditorType {

    @Comment("语言")
    String language();

    @Comment("编辑器高度")
    int height() default 300;

    @Comment("提示触发字符")
    String[] triggerCharacters() default "$";

    @Comment("代码提示处理类参数")
    String[] hintParams() default {};

    @Comment("代码提示处理类")
    Class<CodeEditHintHandler> hint() default CodeEditHintHandler.class;

}
