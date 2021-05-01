package xyz.erupt.annotation.sub_field;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2021/3/22 10:13
 */
public @interface ReadOnly {
    Enum[] value() default {Enum.ADD, Enum.EDIT};

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends ReadOnly.ReadOnlyHandler> exprHandler() default ReadOnly.ReadOnlyHandler.class;

    enum Enum {
        ADD, EDIT
    }

    interface ReadOnlyHandler {

        Enum[] handler(Enum[] sceneEnum, String[] params);

    }
}
