package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.sub_field.Edit;

/**
 * Created by liyuepeng on 11/8/18.
 */
public @interface CodeAndEdit {
    String code();

    String codeType() default "";

    Edit edit();
}
