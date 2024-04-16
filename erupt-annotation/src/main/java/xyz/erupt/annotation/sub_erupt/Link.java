package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2020-03-08
 */
public @interface Link {

    @Comment("erupt class to associate with. Note that this class needs to be configured with access permissions")
    Class<?> linkErupt();

    @Transient
    String column() default "id";

    @Transient
    @Comment("Column in linkErupt → this.column = linkErupt.joinColumn")
    String joinColumn();
}
