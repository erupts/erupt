package xyz.erupt.annotation;

import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.*;

import java.beans.Transient;
import java.lang.annotation.*;
import java.util.Date;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface Erupt {

    String[] index5() default {"!14123", "!2411234", "1234134"};

//    byte[] index() default {'A','C','B','%'};

    byte index8() default 'A';

    char[] index7() default {'A', 'C', 'A', 'B'};

    double[] index2() default {2333.12312D, 132131.12312412D};

    Class<? extends Annotation>[] annotation() default {Erupt.class};


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

    RowOperation[] rowOperation() default {};

    Filter filter() default @Filter(condition = "");

    Tree tree() default @Tree(id = "id", label = "name");

    Class<? extends DataProxy>[] dateProxy() default {};

    KV[] params() default {};
}
