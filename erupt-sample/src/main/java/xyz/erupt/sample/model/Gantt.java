package xyz.erupt.sample.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.Vis;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.vis.CardView;
import xyz.erupt.annotation.vis.GanttView;
import xyz.erupt.jpa.model.BaseModel;

import java.util.Date;

@Erupt(name = "Gantt",
        vis = {
                @Vis(
                        type = Vis.Type.CARD, title = "Card",
                        cardView = @CardView
                ),
                @Vis(
                        type = Vis.Type.GANTT, title = "Gantt",
                        ganttView = @GanttView(startDateField = "start", endDateField = "end")
                )
        }
)
@Table(name = "t_gantt")
@Entity
public class Gantt extends BaseModel {

    @EruptField(
            views = @View(title = "Text"),
            edit = @Edit(title = "Text", search = @Search(vague = true))
    )
    private String input;

    @EruptField(
            views = @View(title = "Color"),
            edit = @Edit(title = "Color", type = EditType.COLOR)
    )
    private String color;

    @EruptField(
            views = @View(title = "Start"),
            edit = @Edit(title = "Start", search = @Search)
    )
    private Date start;

    @EruptField(
            views = @View(title = "End"),
            edit = @Edit(title = "End", search = @Search)
    )
    private Date end;

}
