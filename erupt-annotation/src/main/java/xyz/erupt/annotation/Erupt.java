package xyz.erupt.annotation;

import xyz.erupt.annotation.config.ToMap;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tree;

import java.beans.Transient;
import java.lang.annotation.*;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface Erupt {

    String primaryKeyCol() default "id";

    @Transient
    boolean loginUse() default true;

    @Transient
    String name();

    @Transient
    String desc() default "";

    @Transient
    String[] sorts() default {};

    Power power() default @Power;

    //@ToMap(key = "code")
    RowOperation[] rowOperation() default {};

    Filter filter() default @Filter(condition = "");

    Tree tree() default @Tree(id = "id", label = "name");

    Class<? extends DataProxy>[] dateProxy() default {};

    @ToMap(key = "key")
    KV[] param() default {};
}
