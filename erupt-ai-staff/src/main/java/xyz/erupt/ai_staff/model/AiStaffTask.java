package xyz.erupt.ai_staff.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_staff.proxy.AiStaffTaskProxy;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * A work order assigned to an AI staff member; runs on a cron schedule or on demand.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Erupt(
        name = "Staff Task",
        dataProxy = AiStaffTaskProxy.class,
        drills = @Drill(title = "Work Log", icon = "fa fa-file-text-o",
                link = @Link(linkErupt = AiStaffTaskLog.class, joinColumn = "taskId")),
        rowOperation = @RowOperation(code = "execute", icon = "fa fa-play", title = "Execute Now",
                mode = RowOperation.Mode.SINGLE, operationHandler = AiStaffTaskProxy.class)
)
@Table(name = "e_ai_staff_task")
@Getter
@Setter
@Entity
@EruptI18n
public class AiStaffTask extends MetaModelUpdateVo {

    @ManyToOne
    @JoinColumn(name = "staff_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Staff", column = "name"),
            edit = @Edit(title = "Staff", notNull = true, type = EditType.REFERENCE_TABLE, search = @Search)
    )
    private AiStaff staff;

    @EruptField(
            views = @View(title = "Task Name"),
            edit = @Edit(title = "Task Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Task Instruction"),
            edit = @Edit(title = "Task Instruction", notNull = true, type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "markdown"))
    )
    private String instruction;

    @EruptField(
            views = @View(title = "Cron Expression", width = "150px"),
            edit = @Edit(title = "Cron Expression", desc = "Leave blank to run manually only")
    )
    private String cron;

    @ManyToOne
    @JoinColumn(name = "channel_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Report To", column = "name"),
            edit = @Edit(title = "Report To", type = EditType.REFERENCE_TABLE,
                    desc = "Channel the work report is pushed to after each run")
    )
    private AiStaffChannel channel;

    @EruptField(
            views = @View(title = "Task Status"),
            edit = @Edit(title = "Task Status", notNull = true, search = @Search,
                    boolType = @BoolType(trueText = "Enable", falseText = "Disable"))
    )
    private Boolean enable = true;

}
