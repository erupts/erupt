package xyz.erupt.annotation.sub_erupt;

/**
 * @author YuePeng
 * date 2022/10/9 23:52
 */
public @interface CardView {

    int a = 1;

    boolean enable() default true;

    String imageField() default "";

    String[] viewFields() default {};

}
