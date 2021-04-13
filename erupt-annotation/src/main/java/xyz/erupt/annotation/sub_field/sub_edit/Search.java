package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public @interface Search {

    boolean value() default true;

    @Comment("高级查询")
    boolean vague() default false;

    @Comment("是否必填")
    boolean notNull() default false;

}
