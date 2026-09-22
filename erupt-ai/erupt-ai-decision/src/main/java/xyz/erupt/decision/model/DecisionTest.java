package xyz.erupt.decision.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.sub_edit.ButtonType;
import xyz.erupt.annotation.sub_field.sub_edit.TextareaType;
import xyz.erupt.decision.handler.DecisionTestHandler;
import xyz.erupt.jpa.model.BaseModel;

/**
 * The dialog behind a decision's test button: a state to judge, and the answers it produced
 * kept on screen beside it, so a rubric is read against the content that exercised it.
 *
 * @author YuePeng
 */
@Erupt(name = "Decision Test")
@Getter
@Setter
@EruptI18n
public class DecisionTest extends BaseModel {

    // Filled in from the row the button was clicked on
    @EruptField(
            edit = @Edit(title = "Decision Code", readonly = @Readonly, show = false)
    )
    private String code;

    @EruptField(
            edit = @Edit(title = "Test State", notNull = true, type = EditType.TEXTAREA,
                    desc = "The content to judge. Plain text, or json to give it structure",
                    textareaType = @TextareaType(minRows = 4))
    )
    private String state;

    @EruptField(
            edit = @Edit(title = "Test Run", type = EditType.BUTTON,
                    buttonType = @ButtonType(fullSpan = true, icon = "fa fa-bolt", handler = DecisionTestHandler.class))
    )
    private String run;

    // Written back by the button's populateForm, never submitted
    @EruptField(
            edit = @Edit(title = "Answers", type = EditType.TEXTAREA, readonly = @Readonly,
                    textareaType = @TextareaType(minRows = 6))
    )
    private String answers;

}
