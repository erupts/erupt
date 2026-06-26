package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public @interface Search {

    boolean value() default true;

    @Comment("Whether the field is required")
    boolean notNull() default false;

}
