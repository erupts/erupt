package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author YuePeng
 * date 2018-09-18.
 */
public @interface BoolType {
    String trueText() default "Y";

    String falseText() default "N";
}
