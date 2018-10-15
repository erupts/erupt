package com.erupt.annotation;

import com.erupt.annotation.sub_field.Edit;
import com.erupt.annotation.sub_field.Search;
import com.erupt.annotation.sub_field.View;

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
    Edit edit() default @Edit;

    //可用于搜索的列信息
    Search search() default @Search;

}
