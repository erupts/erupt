package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2026-06-10.
 */
public @interface GroupType {

    @Comment("Field names to be grouped")
    String[] fields();

    @Comment("Whether the group is initially collapsed")
    boolean collapsed() default false;
}
