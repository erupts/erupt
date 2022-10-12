package xyz.erupt.annotation.sub_erupt;

/**
 * 画册视图
 * @author YuePeng
 * date 2022/10/9 23:52
 */
public @interface CardView {

    boolean enable() default true;

    String imageField() default "";

    String[] viewFields();

}
