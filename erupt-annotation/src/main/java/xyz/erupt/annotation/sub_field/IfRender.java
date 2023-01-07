package xyz.erupt.annotation.sub_field;

import java.beans.Transient;

public @interface IfRender {

    boolean value() default true;

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends RenderHandler> renderHandler() default RenderHandler.class;

    interface RenderHandler {

        /**
         * @param expr   表达式
         * @param params 注解参数
         * @return 程序处理后的表达式
         */
        boolean render(boolean expr, String[] params);
    }

}
