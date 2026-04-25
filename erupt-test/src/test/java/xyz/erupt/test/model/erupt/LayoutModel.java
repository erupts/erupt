package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Layout;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * 演示 @Layout：后端分页（每页20条）、表单铺满整行、左右各固定1列
 */
@Getter
@Setter
@Entity
@Erupt(name = "Layout - Backend Paging",
        layout = @Layout(
                pagingType = Layout.PagingType.BACKEND,
                pageSize = 20,
                pageSizes = {10, 20, 50, 100},
                formSize = Layout.FormSize.DEFAULT,
                tableLeftFixed = 1,
                tableRightFixed = 1
        )
)
public class LayoutModel extends BaseModel {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "Code"),
            edit = @Edit(title = "Code")
    )
    private String code;

    @EruptField(
            views = @View(title = "Category"),
            edit = @Edit(title = "Category")
    )
    private String category;

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
