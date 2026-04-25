package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
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
@Erupt(name = "TabTreeEdit")
public class TabTreeModel extends BaseModel {

    @ManyToMany
    @EruptField(
            views = @View(title = "Refs"),
            edit = @Edit(title = "Refs", type = EditType.TAB_TREE)
    )
    private List<RefTargetModel> refs;
}
