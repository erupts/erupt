package xyz.erupt.annotation.sub_field;

import java.beans.Transient;

public @interface IfRender {

    boolean value() default true;

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends RenderHandler> renderHandler() default RenderHandler.class;

    interface RenderHandler {

        boolean render(boolean expr, String[] params);
    }

}
