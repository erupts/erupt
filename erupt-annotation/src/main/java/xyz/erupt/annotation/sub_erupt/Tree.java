package xyz.erupt.annotation.sub_erupt;

/**
 * @author liyuepeng
 * @date 2019-11-13.
 */
public @interface Tree {

    String id();

    String label();

    String pid() default "";
}
