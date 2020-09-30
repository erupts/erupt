package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.expr.Expr;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2019-11-13.
 */
public @interface Tree {

    String id() default AnnotationConst.ID;

    String label() default AnnotationConst.LABEL;

    String pid() default "";

    @Transient
    Expr rootTagExpr() default @Expr;

}
