package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "ReferenceTableEdit")
public class ReferenceTableModel extends BaseModel {

    // basic table reference
    @ManyToOne
    @EruptField(
            views = @View(title = "Category", column = "name"),
            edit = @Edit(title = "Category", type = EditType.REFERENCE_TABLE,
                    referenceTableType = @ReferenceTableType(id = "id", label = "name"),
                    search = @Search)
    )
    private RefTargetModel category;

    // sorted by name
    @ManyToOne
    @EruptField(
            views = @View(title = "Owner", column = "name"),
            edit = @Edit(title = "Owner", type = EditType.REFERENCE_TABLE,
                    orderBy = "name asc",
                    referenceTableType = @ReferenceTableType(id = "id", label = "name"))
    )
    private RefTargetModel owner;
}
