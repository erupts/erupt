package xyz.erupt.annotation.sub_field.sub_edit;

import java.beans.Transient;

/**
 * Created by liyuepeng on 10/11/18.
 */
public @interface VL {

    String value();

    String label();

    @Transient
    String desc() default "";
}
