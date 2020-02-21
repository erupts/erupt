package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.EruptProperty;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2018-09-28.
 */
public @interface View {

    String title();

    //在修饰类型为实体类对象时必须指定列名
    String column() default "";

    @EruptProperty(alias = "viewType")
    ViewType type() default ViewType.AUTO;

    boolean show() default true;

    boolean sortable() default false;

    @Transient
    boolean export() default true;

    String className() default "";

    /**
     * 该参数在前端使用eval方法解析
     * 支持变量：
     * 1、item（整行数据）
     * 2、item.xxx（数据中的某一列）
     * demo："姓名：" + item.name  => 姓名xxxx
     */
    String template() default "";

}
