package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.CodeEditHintHandler;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2020-02-15
 */
public @interface CodeEditorType {

    @Comment("Language")
    String language();

    @Comment("Editor height")
    int height() default 300;

    @Deprecated
    @Comment("Hint trigger characters")
    String[] triggerCharacters() default "$";

    @Deprecated
    @Comment("Code hint handler parameters")
    @Transient
    String[] hintParams() default {};

    @Deprecated
    @Comment("Code hint handler class")
    @Transient
    Class<CodeEditHintHandler> hint() default CodeEditHintHandler.class;

}
