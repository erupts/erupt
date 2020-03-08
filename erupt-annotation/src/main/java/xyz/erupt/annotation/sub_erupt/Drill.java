package xyz.erupt.annotation.sub_erupt;

/**
 * @author liyuepeng
 * @date 2020-02-17.
 */
public @interface Drill {

    String code();

    String title();

    String icon() default "";

    Link link();
}
