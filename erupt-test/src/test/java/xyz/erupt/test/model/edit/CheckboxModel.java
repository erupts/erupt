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
import xyz.erupt.annotation.sub_field.sub_edit.CheckboxType;
import xyz.erupt.jpa.model.BaseModel;

import java.util.List;

@Getter
@Setter
@Entity
@Erupt(name = "CheckboxEdit")
public class CheckboxModel extends BaseModel {

    // 基础：id + label
    @ManyToMany
    @EruptField(
            views = @View(title = "Tags"),
            edit = @Edit(title = "Tags", type = EditType.CHECKBOX,
                    checkboxType = @CheckboxType(id = "id", label = "name"))
    )
    private List<RefTargetModel> tags;

    // 带 remark（描述列）
    @ManyToMany
    @EruptField(
            views = @View(title = "Roles"),
            edit = @Edit(title = "Roles", type = EditType.CHECKBOX,
                    checkboxType = @CheckboxType(id = "id", label = "name", remark = "description"))
    )
    private List<RefTargetModel> roles;
}
