package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author YuePeng
 * date 2023-03-23
 */
public @interface RateType {

    // Custom characters
    String character() default "";

    // Is it allowed to have a partial selection?
    boolean allowHalf() default false;

    // Total number of characters
    int count() default 5;

}
