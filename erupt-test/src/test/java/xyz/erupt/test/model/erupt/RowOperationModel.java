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
 * 演示 @RowOperation 自定义操作按钮：
 * - BUTTON 模式：不依赖行数据，顶部独立按钮
 * - SINGLE 模式：选中单行后可用
 * - MULTI 模式：选中多行后可用（也支持单行）
 * - MULTI_ONLY 模式：仅多选时展示，隐藏单行操作按钮
 */
@Getter
@Setter
@Entity
@Erupt(name = "RowOperation Demo",
        rowOperation = {
                // 不依赖行选中，顶部独立按钮
                @RowOperation(
                        code = "import",
                        title = "Custom Import",
                        icon = "fa fa-upload",
                        mode = RowOperation.Mode.BUTTON,
                        callHint = "",
                        operationHandler = TestOperationHandler.class
                ),
                // 选中单行才可点击
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
                // 选中多行可批量操作
                @RowOperation(
                        code = "batchDelete",
                        title = "Batch Archive",
                        icon = "fa fa-archive",
                        mode = RowOperation.Mode.MULTI,
                        operationHandler = TestOperationHandler.class
                ),
                // 仅多选时展示，单选时隐藏
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
