package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * Demonstrates @RowOperation custom action buttons:
 * - BUTTON: standalone top button, no row selection required
 * - SINGLE: enabled when exactly one row is selected
 * - MULTI: enabled when one or more rows are selected
 * - MULTI_ONLY: shown only when multiple rows are selected
 */
@Getter
@Setter
@Entity
@Erupt(name = "RowOperation Demo",
        rowOperation = {
                // standalone top button, no row selection required
                @RowOperation(
                        code = "import",
                        title = "Custom Import",
                        icon = "fa fa-upload",
                        mode = RowOperation.Mode.BUTTON,
                        callHint = "",
                        operationHandler = TestOperationHandler.class
                ),
                // requires single-row selection
                @RowOperation(
                        code = "approve",
                        title = "Approve",
                        icon = "fa fa-check",
                        tip = "Approve selected record",
                        mode = RowOperation.Mode.SINGLE,
                        ifExpr = "item.status === 'PENDING'",
                        ifExprBehavior = RowOperation.IfExprBehavior.DISABLE,
                        operationHandler = TestOperationHandler.class
                ),
                // batch operation on multiple selected rows
                @RowOperation(
                        code = "batchDelete",
                        title = "Batch Archive",
                        icon = "fa fa-archive",
                        mode = RowOperation.Mode.MULTI,
                        operationHandler = TestOperationHandler.class
                ),
                // shown only when multiple rows are selected
                @RowOperation(
                        code = "export",
                        title = "Export Selected",
                        icon = "fa fa-download",
                        mode = RowOperation.Mode.MULTI_ONLY,
                        callHint = "",
                        operationHandler = TestOperationHandler.class
                )
        }
)
public class RowOperationModel extends BaseModel {

    @EruptField(
            views = @View(title = "Title"),
            edit = @Edit(title = "Title", notNull = true)
    )
    private String title;

    @EruptField(
            views = @View(title = "Status"),
            edit = @Edit(title = "Status")
    )
    private String status;

    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark")
    )
    private String remark;
}
