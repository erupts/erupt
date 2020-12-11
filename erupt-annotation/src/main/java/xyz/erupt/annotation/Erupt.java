package xyz.erupt.annotation;

import xyz.erupt.annotation.config.Match;
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
public @interface Erupt {

    String primaryKeyCol() default AnnotationConst.ID;

    @Transient
    String name();

    @Transient
    String desc() default AnnotationConst.EMPTY_STR;

    @Transient
    boolean authVerify() default true;

    @Transient
    Power power() default @Power;

    @ToMap(key = "code")
    RowOperation[] rowOperation() default {};

    @ToMap(key = "code")
    Drill[] drills() default {};

    @Transient
    Filter[] filter() default {};

    @Transient
    String orderBy() default "";

    @Transient
    Class<? extends DataProxy>[] dataProxy() default {};

    Tree tree() default @Tree;

    @Match("value.field()")
    LinkTree linkTree() default @LinkTree(field = AnnotationConst.EMPTY_STR);

    @ToMap(key = "key")
    KV[] param() default {};

    Class<? extends Annotation> extra() default Annotation.class;
}
