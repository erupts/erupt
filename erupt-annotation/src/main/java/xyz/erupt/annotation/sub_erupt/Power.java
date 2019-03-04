package xyz.erupt.annotation.sub_erupt;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface Power {
    boolean add() default true;

    boolean delete() default true;

    boolean edit() default true;

    boolean query() default true;

    boolean export() default false;

    boolean importable() default false;
}
