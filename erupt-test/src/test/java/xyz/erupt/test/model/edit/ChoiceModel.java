package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "ChoiceEdit")
public class ChoiceModel extends BaseModel {

    // dropdown selection (default SELECT)
    @EruptField(
            views = @View(title = "Status"),
            edit = @Edit(title = "Status", type = EditType.CHOICE,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = "0", label = "Draft"),
                            @VL(value = "1", label = "Published"),
                            @VL(value = "2", label = "Archived")
                    }),
                    search = @Search)
    )
    private String status;

    // VL with color indicators
    @EruptField(
            views = @View(title = "Priority"),
            edit = @Edit(title = "Priority", type = EditType.CHOICE,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = "LOW", label = "Low", color = "#52c41a"),
                            @VL(value = "MEDIUM", label = "Medium", color = "#faad14"),
                            @VL(value = "HIGH", label = "High", color = "#f5222d")
                    }))
    )
    private String priority;

    // VL with disabled item + description
    @EruptField(
            views = @View(title = "Level"),
            edit = @Edit(title = "Level", type = EditType.CHOICE,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = "A", label = "Level A", desc = "Top tier"),
                            @VL(value = "B", label = "Level B"),
                            @VL(value = "C", label = "Level C", disable = true)
                    }))
    )
    private String level;

    // radio buttons (RADIO layout)
    @EruptField(
            views = @View(title = "Gender"),
            edit = @Edit(title = "Gender", type = EditType.CHOICE,
                    choiceType = @ChoiceType(type = ChoiceType.Type.RADIO, vl = {
                            @VL(value = "M", label = "Male"),
                            @VL(value = "F", label = "Female"),
                            @VL(value = "O", label = "Other")
                    }))
    )
    private String gender;

    // dynamic handler for options (re-fetched on edit)
    @EruptField(
            views = @View(title = "Category"),
            edit = @Edit(title = "Category", type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            fetchHandler = {TestChoiceFetchHandler.class}
                    ))
    )
    private String category;
}
