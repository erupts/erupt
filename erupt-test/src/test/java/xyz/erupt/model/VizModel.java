package xyz.erupt.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.Viz;
import xyz.erupt.annotation.viz.CardView;
import xyz.erupt.annotation.viz.GanttView;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "Viz", viz = {
        @Viz(title = "test", fields = {"id", "name"}, type = Viz.Type.CARD,
                cardView = @CardView(coverField = "name"),
                ganttView = @GanttView(startDateField = "a",endDateField = "b",progressField = "c",colorField = "c.c.c")
        )
})
public class VizModel extends BaseModel {


}
