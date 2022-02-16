package xyz.erupt.annotation.sub_field;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2021/3/22 10:13
 */
public @interface Readonly {

    boolean add() default true;

    boolean edit() default true;

    @Transient
    String[] params() default {};

    @Transient
    Class<? extends ReadonlyHandler> exprHandler() default ReadonlyHandler.class;

    interface ReadonlyHandler {

        boolean add(boolean add, String[] params);

        boolean edit(boolean edit, String[] params);

    }
}
