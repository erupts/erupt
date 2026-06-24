package xyz.erupt.annotation.sub_erupt;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.expr.Expr;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2019-11-13.
 */
public @interface Tree {

    @Comment("Tree storage column")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String id() default AnnotationConst.ID;

    @Comment("Tree display column")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String label() default AnnotationConst.LABEL;

    @Comment("Parent node identifier column")
    @Language(value = "hql", prefix = "select ", suffix = " from")
    String pid() default "";

    @Comment("Number of expanded levels")
    int expandLevel() default 999;

    @Transient
    @Comment("Identifies what characteristic of pid marks a root node; defaults to null as the root if not configured; must be used together with filter")
    Expr rootPid() default @Expr;

}
