package xyz.erupt.annotation;

import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.*;
import xyz.erupt.annotation.sub_erupt.*;

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

    String primaryKeyCol() default "id";

    String name() default "";

    String desc() default "";

    String[] sorts() default {};

    Power power() default @Power;

    RowOperation[] rowOperation() default {};

    Filter filter() default @Filter(condition = "");

    Tree tree() default @Tree(id = "id", label = "name");

    Class<? extends DataProxy>[] dateProxy() default {};
}
