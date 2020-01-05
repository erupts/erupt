package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.fun.PowerHandler;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2018-09-28.
 */
public @interface Power {
    boolean add() default true;

    boolean delete() default true;

    boolean edit() default true;

    boolean query() default true;

    boolean viewDetails() default true;

    @Transient
    Class<? extends PowerHandler> powerHandler() default PowerHandler.class;

    boolean export() default false;

    boolean importable() default false;
}
