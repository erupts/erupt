package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
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
@Erupt(name = "DivideEdit")
public class DivideModel extends BaseModel {

    // section 1
    @Transient
    @EruptField(
            edit = @Edit(title = "Basic Info", type = EditType.DIVIDE)
    )
    private String divide1;

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "Email"),
            edit = @Edit(title = "Email")
    )
    private String email;

    // section 2
    @Transient
    @EruptField(
            edit = @Edit(title = "Extra Info", type = EditType.DIVIDE)
    )
    private String divide2;

    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark")
    )
    private String remark;

    @EruptField(
            views = @View(title = "Sort"),
            edit = @Edit(title = "Sort")
    )
    private Integer sort;
}
