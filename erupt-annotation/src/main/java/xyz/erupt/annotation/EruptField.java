package xyz.erupt.annotation;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.ToMap;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;

import java.beans.Transient;
import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface EruptField {

    @Comment("Table column configuration")
    View[] views() default {};

    @Comment("Edit component configuration")
    Edit edit() default @Edit(title = "");

    @Transient
    @Comment("Display order")
    int sort() default 1000;

    @ToMap(key = "key")
    @Comment("Custom extension parameters")
    KV[] params() default {};
}
