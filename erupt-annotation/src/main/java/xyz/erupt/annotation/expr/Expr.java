package xyz.erupt.annotation.expr;

import java.beans.Transient;

public @interface Expr {

    String value() default "";

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends ExprHandler> exprHandler() default ExprHandler.class;

    interface ExprHandler {

        String handler(String expr, String[] params);
    }
}
