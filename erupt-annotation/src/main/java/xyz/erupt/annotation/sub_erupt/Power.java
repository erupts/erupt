package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.fun.PowerHandler;

import java.beans.Transient;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface Power {
    boolean add() default true;

    boolean delete() default true;

    boolean edit() default true;

    boolean query() default true;

    boolean viewDetails() default true;

    @Transient
    Class<? extends PowerHandler>[] powerHandler() default {};

    boolean export() default false;

    boolean importable() default false;
}
