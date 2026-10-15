package xyz.erupt.test.scope.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.test.model.edit.RefTargetModel;

@Getter
@Setter
@Entity
@EruptScope(TargetScope.class)
@Erupt(name = "Target scoped task", authVerify = false)
public class TargetScopedTask extends BaseModel {

    @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", notNull = true))
    private String name;

    @ManyToOne
    @EruptField(views = @View(title = "Target", column = "name"), edit = @Edit(title = "Target", notNull = true,
            type = EditType.REFERENCE_TABLE, referenceTableType = @ReferenceTableType(label = "name")))
    private RefTargetModel target;

}
