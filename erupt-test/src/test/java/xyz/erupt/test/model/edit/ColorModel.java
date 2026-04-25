package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
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
@Erupt(name = "ColorEdit")
public class ColorModel extends BaseModel {

    @EruptField(
            views = @View(title = "Value"),
            edit = @Edit(title = "Value", type = EditType.COLOR)
    )
    private String value;
}
