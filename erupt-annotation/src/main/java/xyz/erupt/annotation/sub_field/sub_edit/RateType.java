package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author YuePeng
 * date 2023-03-23
 */
public @interface RateType {

    //自定义字符
    String character() default "";

    //是否允许半选
    boolean allowHalf() default false;

    //star 总数
    int count() default 5;

}
