package xyz.erupt.ai_staff.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.model.EruptUser;

/**
 * A digital employee: an AI worker with its own system account (identity and
 * tool permissions), a duty description, and an optional dedicated model.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Erupt(
        name = "AI Staff",
        drills = @Drill(title = "Tasks", icon = "fa fa-tasks",
                link = @Link(linkErupt = AiStaffTask.class, joinColumn = "staff.id"))
)
@Table(name = "e_ai_staff")
@Getter
@Setter
@Entity
@EruptI18n
public class AiStaff extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Position"),
            edit = @Edit(title = "Position", search = @Search(operator = QueryExpression.LIKE))
    )
    private String position;

    @EruptField(
            views = @View(title = "Status"),
            edit = @Edit(title = "Status", notNull = true, search = @Search,
                    boolType = @BoolType(trueText = "On Duty", falseText = "Off Duty"))
    )
    private Boolean enable = true;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Staff Account", column = "name"),
            edit = @Edit(title = "Staff Account", notNull = true, type = EditType.REFERENCE_TABLE,
                    desc = "System account the staff works under; its roles decide which AI tools are allowed")
    )
    private EruptUser eruptUser;

    @ManyToOne
    @JoinColumn(name = "llm_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Model", column = "name"),
            edit = @Edit(title = "Model", type = EditType.REFERENCE_TABLE,
                    desc = "Leave blank to use the default chat model")
    )
    private LLM llm;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Duty"),
            edit = @Edit(title = "Duty", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "markdown"),
                    desc = "Responsibilities and persona, appended to the system prompt on every run")
    )
    private String duty;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

}
