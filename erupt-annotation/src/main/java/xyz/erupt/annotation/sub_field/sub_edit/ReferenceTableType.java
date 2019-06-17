package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.sub_erupt.Filter;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface ReferenceTableType {
    String id() default "id";

    Filter filter() default @Filter(condition = "");
}
