package xyz.erupt.ai_dev.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;

/**
 * Form of the retry and request-changes actions: what the agent should do differently next run.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Erupt(name = "Instruction", primaryKeyCol = "instruction")
@Getter
@Setter
@EruptI18n
public class DevInstructionForm {

    @EruptField(
            edit = @Edit(title = "Instruction", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "markdown"),
                    desc = "Sent to the agent with the requirement; it continues its previous session")
    )
    private String instruction;

}
