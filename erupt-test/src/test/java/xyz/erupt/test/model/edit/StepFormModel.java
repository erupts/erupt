package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Layout;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * Step-by-step wizard form: with layout.formSteps enabled,
 * each DIVIDE field starts a new step instead of rendering a divider.
 */
@Getter
@Setter
@Entity
@Erupt(name = "StepForm", layout = @Layout(formSteps = true))
public class StepFormModel extends BaseModel {

    // step 1
    @Transient
    @EruptField(
            edit = @Edit(title = "Basic Info", desc = "Who you are", type = EditType.DIVIDE)
    )
    private String step1;

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "Age"),
            edit = @Edit(title = "Age")
    )
    private Integer age;

    // step 2
    @Transient
    @EruptField(
            edit = @Edit(title = "Contact", desc = "How to reach you", type = EditType.DIVIDE)
    )
    private String step2;

    @EruptField(
            views = @View(title = "Email"),
            edit = @Edit(title = "Email", notNull = true)
    )
    private String email;

    @EruptField(
            views = @View(title = "Phone"),
            edit = @Edit(title = "Phone")
    )
    private String phone;

    // step 3
    @Transient
    @EruptField(
            edit = @Edit(title = "Confirm", desc = "Final remarks", type = EditType.DIVIDE)
    )
    private String step3;

    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

}
