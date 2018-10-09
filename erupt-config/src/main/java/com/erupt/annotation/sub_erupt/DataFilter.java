package com.erupt.annotation.sub_erupt;

import com.erupt.annotation.fun.DataFilterHandler;

/**
 * Created by liyuepeng on 10/9/18.
 */
public @interface DataFilter {

    String by() default "";

    Class<? extends DataFilterHandler> dateFilter()[] default {};
}
