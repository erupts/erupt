package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

import java.util.List;

@Getter
@Setter
@Entity
@Erupt(name = "TabTableAddEdit")
public class TabTableAddModel extends BaseModel {

    @OneToMany
    @EruptField(
            views = @View(title = "Children"),
            edit = @Edit(title = "Children", type = EditType.TAB_TABLE_ADD)
    )
    private List<RefTargetModel> children;
}
