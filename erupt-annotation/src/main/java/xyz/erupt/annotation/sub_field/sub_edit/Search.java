package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author liyuepeng
 * @date 2018-09-28.
 */
public @interface Search {

    boolean value() default true;

    @Deprecated
    boolean notNull() default false;

    @Comment("高级查询")
    boolean vague() default false;

}
