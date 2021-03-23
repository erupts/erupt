package xyz.erupt.annotation.sub_field;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2021/3/22 10:13
 */
public @interface ReadOnly {
    BehaviorEnum value() default BehaviorEnum.NONE;

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends ReadOnly.ReadOnlyHandler> exprHandler() default ReadOnly.ReadOnlyHandler.class;

    interface ReadOnlyHandler {

        BehaviorEnum handler(BehaviorEnum behaviorEnum, String[] params);

    }
}
