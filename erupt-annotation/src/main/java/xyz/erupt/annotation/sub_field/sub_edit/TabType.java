package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 10/17/18.
 */
public @interface TabType {
    String icon() default "";

    String filter() default "";

    TabEnum type() default TabEnum.LIST_SELECT;
}
