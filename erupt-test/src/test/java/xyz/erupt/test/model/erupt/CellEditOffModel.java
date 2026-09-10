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
 * A model that opted out of cell editing, used to prove the server rejects a crafted
 * single-cell update rather than relying on the client not to offer one.
 */
@Getter
@Setter
@Entity
@Erupt(name = "CellEdit Off", authVerify = false, power = @Power(cellEdit = false))
public class CellEditOffModel extends BaseModel {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name")
    )
    private String name;

}
