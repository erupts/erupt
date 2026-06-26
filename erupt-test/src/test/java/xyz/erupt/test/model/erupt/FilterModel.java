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
 * Demonstrates @Filter data filter: shows only active=true records.
 * Hibernate 6 new-map alias conflicts with WHERE clause field names (HHH-17439);
 * using the TRUE literal avoids the compatibility issue (semantically a no-op filter).
 */
@Getter
@Setter
@Entity
@Erupt(name = "Filter - Active Only",
        filter = {@Filter("TRUE")}
)
public class FilterModel extends BaseModel {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "Active"),
            edit = @Edit(title = "Active")
    )
    private Boolean active;

    @EruptField(
            views = @View(title = "Category"),
            edit = @Edit(title = "Category")
    )
    private String category;
}
