package com.erupt.annotation.sub_field;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface View {

    //在修饰类型为实体类对象时指定列名
    String column() default "";

    String title();

    boolean show() default true;

    int sort() default 0;

}
