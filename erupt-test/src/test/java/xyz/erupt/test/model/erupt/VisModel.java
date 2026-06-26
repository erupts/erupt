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
 * Demonstrates @Vis multi-view:
 * - Default table view (visRawTable = true)
 * - Extra TABLE view: show selected fields only (INCLUDE mode)
 * - Extra TABLE view: exclude specific fields (EXCLUDE mode)
 */
@Getter
@Setter
@Entity
@Erupt(name = "Vis (Multi View) Demo",
        visRawTable = true,
        vis = {
                // summary view: show name and status only
                @Vis(
                        code = "summary",
                        title = "Summary View",
                        fieldVisibility = Vis.FieldVisibility.INCLUDE,
                        fields = {"name", "status"}
                ),
                // detail view: exclude internalNote
                @Vis(
                        code = "detail",
                        title = "Detail View",
                        fieldVisibility = Vis.FieldVisibility.EXCLUDE,
                        fields = {"internalNote"}
                ),
                // kanban board: grouped by status, drag-and-drop supported
                @Vis(
                        code = "board",
                        title = "Kanban Board",
                        type = Vis.Type.BOARD,
                        boardView = @BoardView(
                                groupField = "status"
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
