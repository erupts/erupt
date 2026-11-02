package xyz.erupt.ai_dev.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;

/**
 * Form of the approve and reject actions: the reviewer's comment, required to reject.
 *
 * @author YuePeng
 * date 2026/10/24
 */
@Erupt(name = "Review", primaryKeyCol = "comment")
@Getter
@Setter
@EruptI18n
public class DevReviewForm {

    @EruptField(
            edit = @Edit(title = "Comment", type = EditType.TEXTAREA, inputType = @InputType(length = AnnotationConst.REMARK_LENGTH),
                    desc = "Why it is rejected, or a note the agent reads with an approved requirement")
    )
    private String comment;

}
