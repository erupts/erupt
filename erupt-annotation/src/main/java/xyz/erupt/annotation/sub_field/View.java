package xyz.erupt.annotation.sub_field;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface View {

    String title();

    //在修饰类型为实体类对象时必须指定列名
    String column() default "";

    ViewType viewType() default ViewType.TEXT;

    boolean sortable() default false;

    boolean export() default true;

    String className() default "";

    /**
     * 该参数在前端使用eval方法解析
     * 支持变量：
     * 1、item（整行主数据）
     * 2、item.xxx（数据中的某一列）
     * demo："姓名：" + item.name
     */
    String template() default "";

    boolean show() default true;

}
