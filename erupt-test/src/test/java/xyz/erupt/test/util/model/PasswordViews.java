package xyz.erupt.test.util.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUser;

/**
 * A model whose PASSWORD views sit on a field of its own and on one column of a reference, next to a column that is not
 * a password: what the masking of queried rows must tell apart.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Entity
@Erupt(name = "Password views", authVerify = false)
public class PasswordViews extends BaseModel {

    @EruptField(views = @View(title = "Secret", type = ViewType.PASSWORD), edit = @Edit(title = "Secret", type = EditType.PASSWORD))
    private String secret;

    @EruptField(views = @View(title = "Secret note"), edit = @Edit(title = "Secret note"))
    private String secret_note;

    @ManyToOne
    @EruptField(views = {@View(title = "Owner", column = "name"), @View(title = "Owner account", column = "account", type = ViewType.PASSWORD)},
            edit = @Edit(title = "Owner", type = EditType.REFERENCE_TABLE))
    private EruptUser owner;

}
