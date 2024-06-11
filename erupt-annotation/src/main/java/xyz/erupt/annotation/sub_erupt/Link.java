package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2020-03-08
 */
public @interface Link {

    @Transient
    String column() default "id";

    @Comment("erupt class to associate with. Note that this class needs to be configured with access permissions")
    Class<?> linkErupt();

    @Transient
    @Comment("Column in linkErupt → this.column = linkErupt.joinColumn")
    String joinColumn();

    @Comment("Other conditions for the link")
    String linkCondition() default "";
}
