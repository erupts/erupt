package com.erupt.annotation.sub_field;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface View {
    String className() default "";

    ViewType viewType() default ViewType.TEXT;

    //在修饰类型为实体类对象时指定列名
    String column() default "";

    String title();

    /**
     * demo : 网址：@txt@
     * @txt@: 为数据项占位符
     */
    String template() default "";

    boolean show() default true;

    int sort() default 0;

}
