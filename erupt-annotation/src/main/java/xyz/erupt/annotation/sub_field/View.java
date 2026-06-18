package xyz.erupt.annotation.sub_field;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.sub_erupt.Tpl;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public @interface View {

    String title();

    String desc() default "";

    @Comment("Define a popup template for a table column; use the row variable in the template to retrieve the current row's data")
    Tpl tpl() default @Tpl(path = "", enable = false);

    @Comment("Column width (please specify units such as: %, px)")
    String width() default "";

    @Comment("Column name must be specified when the field type is an entity class object")
    String column() default "";

    ViewType type() default ViewType.AUTO;

    @Transient
    @Comment("Dynamic rendering configuration")
    ExprBool ifRender() default @ExprBool;

    boolean show() default true;

    @Comment("Sortable column")
    boolean sortable() default false;

    @Transient
    @Comment("Export column")
    boolean export() default true;

    @Comment("CSS class name")
    @Language("css")
    String className() default "";

    @Comment("Format the table column value; the frontend parses it using the eval method. Supported variables: " +
            "1. item    (entire row data)" +
            "2. item.xxx (value of a specific column in the row)" +
            "3. value   (current column value)")
    @Language("javascript")
    String template() default "";

}
