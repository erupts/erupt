package xyz.erupt.annotation.expr;

import java.beans.Transient;

public @interface ExprBool {

    @Transient
    boolean value() default true;

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends ExprHandler> exprHandler() default ExprHandler.class;

    interface ExprHandler {

        boolean handler(boolean expr, String[] params);
    }
}
