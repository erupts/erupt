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
 * Demonstrates @Power permission config: export + import enabled, print disabled.
 */
@Getter
@Setter
@Entity
@Erupt(name = "Power - Export & Import",
        power = @Power(
                add = true,
                edit = true,
                delete = true,
                query = true,
                export = true,
                importable = true,
                viewDetails = true
        )
)
public class PowerModel extends BaseModel {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "Value"),
            edit = @Edit(title = "Value")
    )
    private String value;
}
