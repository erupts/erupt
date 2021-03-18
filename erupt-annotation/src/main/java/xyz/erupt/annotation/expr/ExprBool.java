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

        /**
         * @param expr   表达式
         * @param params 注解参数
         * @return 程序处理后的表达式
         */
        boolean handler(boolean expr, String[] params);
    }
}
