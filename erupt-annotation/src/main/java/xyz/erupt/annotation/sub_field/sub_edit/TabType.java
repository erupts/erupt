package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.sub_erupt.Filter;

import java.beans.Transient;

/**
 * Created by liyuepeng on 10/17/18.
 */
public @interface TabType {

    @Transient
    String[] sorts() default "";

    Filter filter() default @Filter(condition = "");

    TabEnum type() default TabEnum.TREE;
}
