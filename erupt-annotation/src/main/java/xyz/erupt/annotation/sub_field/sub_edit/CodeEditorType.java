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

    @Comment("Static hint list")
    String[] hints() default {};

    @Comment("Parameters passed to hintHandler")
    @Transient
    String[] hintParams() default {};

    @Comment("Dynamic hint handler class")
    Class<? extends CodeEditHintHandler> hintHandler() default CodeEditHintHandler.class;

}
