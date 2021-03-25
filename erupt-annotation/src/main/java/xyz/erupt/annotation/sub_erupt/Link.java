package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2020-03-08
 */
public @interface Link {

    @Comment("要关联的erupt类，注意：该类需要配置访问权限")
    Class<?> linkErupt();

    @Transient
    @Comment("被关联列，this.joinColumn = linkErupt.column")
    String column() default "id";

    @Transient
    @Comment("需要关联的列，this.joinColumn = linkErupt.column ")
    String joinColumn();
}
