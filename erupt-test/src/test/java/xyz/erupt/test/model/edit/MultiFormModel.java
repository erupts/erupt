package xyz.erupt.test.model.edit;

import jakarta.persistence.CascadeType;
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

/**
 * MULTI_FORM: one-to-many children edited as repeated form blocks. The field is
 * required, so an empty block list must be rejected, and each block is validated
 * against the child model (RefTargetModel.name is required).
 */
@Getter
@Setter
@Entity
// authVerify=false: the CRUD test posts without a menu binding for this model
@Erupt(name = "MultiFormEdit", authVerify = false)
public class MultiFormModel extends BaseModel {

    @EruptField(
            views = @View(title = "Title"),
            edit = @Edit(title = "Title", notNull = true)
    )
    private String title;

    @OneToMany(cascade = CascadeType.ALL)
    @EruptField(
            views = @View(title = "Items"),
            edit = @Edit(title = "Items", type = EditType.MULTI_FORM, notNull = true)
    )
    private List<RefTargetModel> items;
}
