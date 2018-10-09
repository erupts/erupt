package com.erupt.annotation.sub_erupt;

/**
 * 使用一列或者多列的数据执行特定代码
 * Created by liyuepeng on 10/9/18.
 */
public @interface RowOperation {
    String icon() default "";

    String title() default "";

    boolean multi() default false;


}
