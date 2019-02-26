package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.sub_erupt.Filter;

/**
 * Created by liyuepeng on 10/17/18.
 */
public @interface TabType {

    Filter filter() default @Filter(condition = "");

    TabEnum type() default TabEnum.LIST_SELECT;
}
