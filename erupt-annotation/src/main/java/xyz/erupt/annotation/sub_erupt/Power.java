package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.PowerHandler;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public @interface Power {

    boolean add() default true;

    boolean edit() default true;

    boolean delete() default true;

    boolean query() default true;

    boolean viewDetails() default true;

    @Comment("who may export is the role's EXPORT button, columns opt out with @View(export = false)")
    boolean export() default true;

    boolean importable() default false;

    boolean print() default true;

    boolean copy() default true;

    @Comment("Whether rows may be edited one cell at a time, directly in the table. " +
            "A cell runs the same pipeline as the edit form, so turn it off only for a table " +
            "whose rows should always be changed as a reviewed whole")
    boolean cellEdit() default true;

    @Comment("Whether AI tools may inspect and operate on this Erupt model")
    boolean ai() default true;

    @Comment("Whether records of this model carry a comment stream (needs the erupt-comment module)")
    boolean comment() default true;

    @Comment("Whether changes to records of this model are kept as a field-level history with rollback (needs the erupt-revision module)")
    boolean revision() default true;

    @Transient
    @Comment("Dynamic handling of Power permissions")
    Class<? extends PowerHandler> powerHandler() default PowerHandler.class;
}
