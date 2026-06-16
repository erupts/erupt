package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * Demonstrates @Filter dynamic filtering: uses conditionHandler for dynamic conditions
 * such as per-user data isolation.
 */
@Getter
@Setter
@Entity
@Erupt(name = "Filter - Dynamic Handler",
        filter = {@Filter(conditionHandler = TestFilterHandler.class, params = {"userId"})}
)
public class FilterHandlerModel extends BaseModel {

    @EruptField(
            views = @View(title = "Title"),
            edit = @Edit(title = "Title", notNull = true)
    )
    private String title;

    @EruptField(
            views = @View(title = "Owner"),
            edit = @Edit(title = "Owner")
    )
    private String owner;

    @EruptField(
            views = @View(title = "Status"),
            edit = @Edit(title = "Status")
    )
    private String status;
}
