package com.erupt.annotation;

import com.erupt.annotation.fun.DataProxy;
import com.erupt.annotation.sub_erupt.*;

import java.lang.annotation.*;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface Erupt {

    ShowType showType() default ShowType.TABLE;

    boolean loginUse() default true;

    String name() default "";

    String desc() default "";

    Card[] cards() default {};

    Chart[] charts() default {};

    Power power() default @Power;

    RowOperation[] rowOperation() default {};

    Filter filter() default @Filter(condition = "");

    Tree tree() default @Tree(id = "id", label = "name", pid = "pid");

    Class<? extends DataProxy> dateProxy()[] default {};
}
