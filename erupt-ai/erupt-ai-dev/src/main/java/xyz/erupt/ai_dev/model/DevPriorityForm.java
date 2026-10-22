package xyz.erupt.ai_dev.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_dev.constant.Priority;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;

/**
 * Form of the priority action: priority is scheduling, not definition, so it can change in any stage.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Erupt(name = "Priority", primaryKeyCol = "priority")
@Getter
@Setter
@EruptI18n
public class DevPriorityForm {

    @EruptField(edit = @Edit(title = "Priority", notNull = true, type = EditType.CHOICE))
    private Priority priority;

}
