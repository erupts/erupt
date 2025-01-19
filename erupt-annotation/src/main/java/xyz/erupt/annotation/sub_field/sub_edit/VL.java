package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author YuePeng
 * date 2018-10-11.
 */
public @interface VL {

    String value();

    String label();

    boolean disable() default false;

    String desc() default "";
}
