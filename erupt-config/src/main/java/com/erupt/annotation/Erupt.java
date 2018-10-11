package com.erupt.annotation;

import com.erupt.annotation.fun.DataProxy;
import com.erupt.annotation.sub_erupt.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Erupt {

    ShowType showType() default ShowType.TABLE;

    String name();

    Card[] cards() default {};

    Power power() default @Power;

    RowOperation[] rowOperation() default {};

    DataFilter dateFilter() default @DataFilter;

    Class<? extends DataProxy> dateProxy()[] default {};
}
