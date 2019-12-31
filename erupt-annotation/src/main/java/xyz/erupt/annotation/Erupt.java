package xyz.erupt.annotation;

import xyz.erupt.annotation.config.ToMap;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.*;

import java.beans.Transient;
import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2018-09-28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface Erupt {

    String primaryKeyCol() default AnnotationConst.ID;

    String alias() default "";

    @Transient
    String name();

    @Transient
    String desc() default AnnotationConst.EMPTY_STR;

    @Transient
    boolean loginUse() default true;

    Power power() default @Power;

    @ToMap(key = "code")
    RowOperation[] rowOperation() default {};

    @Transient
    Filter filter() default @Filter(condition = "");

    @Transient
    String orderBy() default "";

    @Transient
    Class<? extends DataProxy>[] dataProxy() default {};

    Tree tree() default @Tree(id = AnnotationConst.ID, label = AnnotationConst.LABEL);

    @ToMap(key = "key")
    KV[] param() default {};

    @Transient
    Html beforeHtml() default @Html;

    @Transient
    Html afterHtml() default @Html;

    Class<? extends Annotation> extra() default Annotation.class;
}
