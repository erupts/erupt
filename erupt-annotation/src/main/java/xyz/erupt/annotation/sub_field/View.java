package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.EruptProperty;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public @interface View {

    String title();

    String desc() default "";

    @Comment("列宽度（请指定单位如：%,px）")
    String width() default "";

    @Comment("修饰类型为实体类对象时必须指定列名")
    String column() default "";

    @EruptProperty(alias = "viewType")
    ViewType type() default ViewType.AUTO;

    boolean show() default true;

    boolean sortable() default false;

    @Transient
    boolean export() default true;

    @Comment("样式类名")
    String className() default "";

    @Comment("格式化表格列值，前端使用eval方法解析，支持变量：" +
            "1、item    （表格整行数据）" +
            "2、item.xxx（数据行中某一列的值）" +
            "3、value   （当前列值）")
    String template() default "";

}
