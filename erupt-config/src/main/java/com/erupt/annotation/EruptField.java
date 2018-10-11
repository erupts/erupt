package com.erupt.annotation;

import com.erupt.annotation.sub_field.Edit;
import com.erupt.annotation.sub_field.Search;
import com.erupt.annotation.sub_field.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EruptField {

    //表列信息
    View view() default @View;

    //表多列信息
    View[] multiView() default {};

    //编辑时的列信息
    Edit edit() default @Edit;

    //可用于搜索的列信息
    Search search() default @Search;

}
