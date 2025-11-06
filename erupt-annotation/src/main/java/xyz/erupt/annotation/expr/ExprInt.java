package xyz.erupt.annotation.expr;

import java.beans.Transient;

public @interface ExprInt {

    @Transient
    int value();

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends ExprHandler> exprHandler() default ExprHandler.class;

    interface ExprHandler {

        int handler(int expr, String[] params);
    }
}
