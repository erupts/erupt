package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.Vis;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.vis.BoardView;
import xyz.erupt.jpa.model.BaseModel;

/**
 * 演示 @Vis 多视图：
 * - 默认表格视图（visRawTable = true）
 * - 额外的 TABLE 视图：仅展示部分字段（INCLUDE 模式）
 * - 额外的 TABLE 视图：排除特定字段（EXCLUDE 模式）
 */
@Getter
@Setter
@Entity
@Erupt(name = "Vis (Multi View) Demo",
        visRawTable = true,
        vis = {
                // 摘要视图：只展示 name 和 status
                @Vis(
                        code = "summary",
                        title = "Summary View",
                        fieldVisibility = Vis.FieldVisibility.INCLUDE,
                        fields = {"name", "status"}
                ),
                // 详细视图：排除 internalNote 字段
                @Vis(
                        code = "detail",
                        title = "Detail View",
                        fieldVisibility = Vis.FieldVisibility.EXCLUDE,
                        fields = {"internalNote"}
                ),
                // 看板视图：按 status 分列，支持拖拽移动
                @Vis(
                        code = "board",
                        title = "Kanban Board",
                        type = Vis.Type.BOARD,
                        boardView = @BoardView(
                                groupField = "status",
                                cardTitleField = "name",
                                cardTagField = "category",
                                draggable = true,
                                showCount = true,
                                wipLimit = 0
                        )
                ),
//                @Vis(title = "Card",
//                        type = Vis.Type.CARD,
//                        cardView = @CardView(coverField = "name"),
//                        ganttView = @GanttView(startDateField = "a", endDateField = "b",
//                                progressField = "c", colorField = "c.c.c")
//                )
        }
)
public class VisModel extends BaseModel {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String name;

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
            views = @View(title = "Score"),
            edit = @Edit(title = "Score")
    )
    private Integer score;

    @EruptField(
            views = @View(title = "Internal Note"),
            edit = @Edit(title = "Internal Note")
    )
    private String internalNote;
}
