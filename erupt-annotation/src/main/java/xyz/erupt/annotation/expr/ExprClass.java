package xyz.erupt.annotation.expr;

import java.beans.Transient;

public @interface ExprClass {

    @Transient
    Class<?> value() default void.class;

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends ExprHandler> exprHandler() default ExprHandler.class;

    interface ExprHandler {

        Class<?> handler(Class<?> expr, String[] params);
    }
}
