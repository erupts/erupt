package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Tree;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface ReferenceTreeType {
    String id() default "id";

    String label() default "name";

    String pid() default "";

    String depend() default "";

    String dependColumn() default "id";

    Filter filter() default @Filter(condition = "");
}
