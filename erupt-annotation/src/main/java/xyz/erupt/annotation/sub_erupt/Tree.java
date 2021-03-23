package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.expr.Expr;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2019-11-13.
 */
public @interface Tree {

    @Comment("树存储列")
    String id() default AnnotationConst.ID;

    @Comment("树展示列")
    String label() default AnnotationConst.LABEL;

    @Comment("父级节点标识列")
    String pid() default "";

    @Transient
    @Comment("标识pid为何特征才是根节点，需要与filter配合使用")
    Expr rootPid() default @Expr;

}
