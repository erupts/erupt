package com.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 10/9/18.
 */
public @interface BoolType {
    String trueText();

    String falseText();

    boolean defaultValue() default false;

}
