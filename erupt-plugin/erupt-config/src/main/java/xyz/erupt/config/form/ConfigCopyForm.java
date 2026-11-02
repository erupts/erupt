package xyz.erupt.config.form;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.config.handler.ConfigProfileFetchHandler;

/**
 * @author YuePeng
 */
@Getter
@Setter
@Erupt(name = "Copy to profile", primaryKeyCol = "profile")
public class ConfigCopyForm {

    @EruptField(edit = @Edit(title = "Target profile", notNull = true, type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = ConfigProfileFetchHandler.class)))
    private Long profile;

}
