package xyz.erupt.annotation.sub_field;

/**
 * Created by liyuepeng on 9/28/18.
 */
public @interface View {

    //在修饰类型为实体类对象时，指定列名
    String column() default "";

    String title();

    ViewType viewType() default ViewType.TEXT;

    boolean sortable() default false;

    String className() default "";

    /**
     * demo : xx：@txt@
     *
     * @txt@: 为数据项占位符
     */
    String template() default "";

    boolean show() default true;

}
