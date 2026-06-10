package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2026-06-10.
 */
public @interface CollapseType {

    @Comment("Field names to be grouped under this collapse panel")
    String[] fields() default {};
}
