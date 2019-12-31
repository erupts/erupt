package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * @author liyuepeng
 * @date 2018-09-18.
 */
public @interface BoolType {
    String trueText();

    String falseText();

    boolean defaultValue() default true;

}
