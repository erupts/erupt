package xyz.erupt.ai_tune.model.input;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.jpa.model.BaseModel;

import java.time.LocalDate;

/**
 * Parameters of the "Harvest Chat History" row operation: which AI Chat conversations
 * become training samples. Conversations the user interrupted, or whose answers were
 * produced through tool calls, are skipped because they do not show the final behaviour
 * the tuned model should learn.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Erupt(name = "Harvest Chat History")
@EruptI18n
@Getter
@Setter
public class ChatHarvestForm extends BaseModel {

    @EruptField(
            edit = @Edit(title = "From Date", type = EditType.DATE, dateType = @DateType(type = DateType.Type.DATE))
    )
    private LocalDate fromDate;

    @EruptField(
            edit = @Edit(title = "To Date", type = EditType.DATE, dateType = @DateType(type = DateType.Type.DATE))
    )
    private LocalDate toDate;

    @EruptField(
            edit = @Edit(title = "Model Filter", desc = "Only conversations answered by this model id; blank for all")
    )
    private String model;

    @EruptField(
            edit = @Edit(title = "Min Answer Length", notNull = true, type = EditType.NUMBER,
                    numberType = @NumberType(min = 0), desc = "Characters; shorter assistant answers are dropped")
    )
    private Integer minAnswerLength = 20;

    @EruptField(
            edit = @Edit(title = "Max Conversations", notNull = true, type = EditType.NUMBER,
                    numberType = @NumberType(min = 1, max = 10000))
    )
    private Integer maxConversations = 500;

    @EruptField(
            edit = @Edit(title = "System Prompt", type = EditType.TEXTAREA,
                    desc = "Prepended to every harvested sample as the system message; blank for none")
    )
    private String systemPrompt;

    @EruptField(
            edit = @Edit(title = "One Sample per Turn", type = EditType.BOOLEAN, notNull = true,
                    boolType = @BoolType(trueText = "Per Turn", falseText = "Whole Conversation"),
                    desc = "Per turn: every user/assistant pair becomes its own sample with the preceding turns as context. Whole: one sample per conversation")
    )
    private Boolean perTurn = false;

}
