package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.SceneEnum;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2021/3/22 10:13
 */
public @interface ReadOnly {
    SceneEnum[] value() default {SceneEnum.ADD, SceneEnum.EDIT, SceneEnum.VIEW_DETAIL};

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends ReadOnly.ReadOnlyHandler> exprHandler() default ReadOnly.ReadOnlyHandler.class;

    interface ReadOnlyHandler {

        SceneEnum[] handler(SceneEnum[] sceneEnum, String[] params);

    }
}
