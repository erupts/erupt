package com.erupt.annotation;

import com.erupt.annotation.fun.DataProxy;
import com.erupt.annotation.sub_erupt.DataFilter;
import com.erupt.annotation.sub_field.*;

import java.lang.annotation.*;

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
