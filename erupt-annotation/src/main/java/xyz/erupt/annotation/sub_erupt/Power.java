package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.PowerHandler;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public @interface Power {

    boolean add() default true;

    boolean edit() default true;

    boolean delete() default true;

    boolean query() default true;

    boolean viewDetails() default true;

    boolean export() default false;

    boolean importable() default false;

    @Transient
    @Comment("Dynamic handling of Power permissions")
    Class<? extends PowerHandler> powerHandler() default PowerHandler.class;
}
