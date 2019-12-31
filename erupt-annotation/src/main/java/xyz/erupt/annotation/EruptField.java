package xyz.erupt.annotation;

import xyz.erupt.annotation.config.ToMap;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;

import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2018-09-28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface EruptField {

    //表列信息
    View[] views() default {};

    //编辑信息
    Edit edit() default @Edit(title = AnnotationConst.EMPTY_STR);

    @ToMap(key = "key")
    KV[] params() default {};
}
