package xyz.erupt.annotation.expr;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2021/3/18 11:50
 */
public @interface ExprFloat {

    @Transient
    float value();

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends ExprHandler> exprHandler() default ExprHandler .class;

    interface ExprHandler {

        float handler(float expr, String[] params);
    }
}
