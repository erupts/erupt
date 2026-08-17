package xyz.erupt.ai_staff.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.toolkit.handler.SqlChoiceFetchHandler;

import java.util.Date;

/**
 * Execution record of a staff task, holding the work report produced by the AI staff.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Erupt(
        name = "Staff Work Log",
        orderBy = "startTime desc",
        power = @Power(export = true, add = false, edit = false)
)
@Table(name = "e_ai_staff_task_log")
@Getter
@Setter
@Entity
@EruptI18n
public class AiStaffTaskLog extends BaseModel {

    @EruptField(
            views = @View(title = "Task", type = ViewType.TEXT),
            edit = @Edit(title = "Task", search = @Search, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id, name from e_ai_staff_task"))
    )
    private Long taskId;

    @EruptField(
            views = @View(title = "Staff")
    )
    private String staffName;

    @EruptField(
            views = @View(title = "Exec Status"),
            edit = @Edit(title = "Exec Status", search = @Search,
                    boolType = @BoolType(trueText = "Success", falseText = "Failure"))
    )
    private Boolean status;

    @EruptField(
            views = @View(title = "Start Time", type = ViewType.DATE_TIME),
            edit = @Edit(title = "Start Time", search = @Search)
    )
    private Date startTime;

    @EruptField(
            views = @View(title = "End Time", type = ViewType.DATE_TIME)
    )
    private Date endTime;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Work Report", type = ViewType.MARKDOWN)
    )
    private String report;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Error Info", type = ViewType.HTML)
    )
    private String errorInfo;

}
