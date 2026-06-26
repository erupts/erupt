package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * Demonstrates @Power permission config: read-only, query allowed only.
 */
@Getter
@Setter
@Entity
@Erupt(name = "Power - Read Only",
        power = @Power(
                add = false,
                edit = false,
                delete = false,
                query = true,
                print = false,
                viewDetails = true,
                export = true
        )
)
public class ReadOnlyModel extends BaseModel {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name")
    )
    private String name;

    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark")
    )
    private String remark;
}
