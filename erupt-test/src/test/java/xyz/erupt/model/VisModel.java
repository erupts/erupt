package xyz.erupt.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.Vis;
import xyz.erupt.annotation.vis.CardView;
import xyz.erupt.annotation.vis.GanttView;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "Vis", vis = {
        @Vis(title = "test",
                type = Vis.Type.CARD,
                cardView = @CardView(coverField = "name"),
                ganttView = @GanttView(startDateField = "a", endDateField = "b", progressField = "c", colorField = "c.c.c")
        )
})
public class VisModel extends BaseModel {


}
