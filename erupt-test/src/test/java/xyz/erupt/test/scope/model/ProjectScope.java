package xyz.erupt.test.scope.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

@Getter
@Setter
// A form, not a table: its first field doubles as the key erupt requires
@Erupt(name = "Project", primaryKeyCol = "project")
public class ProjectScope {

    @EruptField(edit = @Edit(title = "Project", type = EditType.CHOICE, notNull = true,
            choiceType = @ChoiceType(vl = {@VL(value = "alpha", label = "Alpha"), @VL(value = "beta", label = "Beta")})))
    private String project;

}
