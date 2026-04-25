package xyz.erupt.annotation.sub_erupt;

import org.intellij.lang.annotations.Language;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.fun.OperationHandler;

import java.beans.Transient;

/**
 * Custom buttons
 *
 * @author YuePeng
 * date 2018-10-09.
 */
public @interface RowOperation {

    String code() default "";

    String title();

    @Transient
    ExprBool show() default @ExprBool;

    @Comment("Feature tooltip")
    String tip() default "";

    @Comment("Confirmation prompt before invocation; no prompt if empty")
    String callHint() default "erupt.operation.call_hint";

    @Comment("Whether to collapse single-row buttons; useful when there are too many buttons")
    boolean fold() default false;

    @Comment("Refer to Font Awesome for icon names")
    @Language(value = "html", prefix = "<i class=\"", suffix = "\"></i>")
    String icon() default "fa fa-dot-circle-o";

    @Comment("Operation mode")
    Mode mode() default Mode.MULTI;

    @Comment("Operation type")
    Type type() default Type.ERUPT;

    @Language("javascript")
    @Comment("Controls button visibility or clickability (JS expression); variable: item retrieves the entire row data")
    String ifExpr() default "";

    @Comment("Controls whether ifExpr result governs button visibility or clickability")
    IfExprBehavior ifExprBehavior() default IfExprBehavior.DISABLE;

    @Comment("Available when type is TPL; the rows variable in the template can be used to retrieve selected row data")
    Tpl tpl() default @Tpl(path = "");

    @Comment("Form information required to be filled in when the button is submitted")
    Class<?> eruptClass() default void.class;

    @Transient
    @Comment("This configuration can be retrieved in operationHandler")
    String[] operationParam() default {};

    @Transient
    @Comment("Available when type is ERUPT; backend processing logic after the operation button is clicked")
    Class<? extends OperationHandler> operationHandler() default OperationHandler.class;

    enum Mode {
        @Comment("Requires single-row data")
        SINGLE,
        @Comment("Requires multi-row data")
        MULTI,
        @Comment("Requires multi-row data only; hides single-row operation button")
        MULTI_ONLY,
        @Comment("Does not depend on row data")
        BUTTON
    }

    enum Type {
        @Comment("Rendered via erupt form; operationHandler handles the logic")
        ERUPT,
        @Comment("Rendered via a custom template")
        TPL
    }

    enum IfExprBehavior {
        @Comment("IfExpr controls button show or hide")
        HIDE,
        @Comment("IfExpr controls whether the button is clickable")
        DISABLE
    }
}
