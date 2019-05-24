package xyz.erupt.annotation;

import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

import java.beans.Transient;
import java.lang.annotation.*;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface EruptField {

    //表列信息
    View[] views() default {};

    //编辑时的列信息
    Edit edit() default @Edit(title = "");

    KV[] params() default {};
}
