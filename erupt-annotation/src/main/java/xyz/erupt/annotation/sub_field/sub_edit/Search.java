package xyz.erupt.annotation.sub_field.sub_edit;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface Search {
    boolean search() default false;

    boolean vague() default false;
}
