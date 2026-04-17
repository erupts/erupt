package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "CombineEdit")
public class CombineModel extends BaseModel {

    @OneToOne
    @EruptField(
            views = @View(title = "Detail", column = "name"),
            edit = @Edit(title = "Detail", type = EditType.COMBINE)
    )
    private RefTargetModel detail;
}
