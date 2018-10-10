package com.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 10/9/18.
 * 字段必须返回DictItem对象才能正常使用
 */
public @interface DictType {
    String dictCode() default "";
}
