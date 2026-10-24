package xyz.erupt.ai_dev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import xyz.erupt.ai_dev.constant.Priority;
import xyz.erupt.ai_dev.constant.RequirementStage;
import xyz.erupt.ai_dev.proxy.DevPriorityOperation;
import xyz.erupt.ai_dev.proxy.DevRequirementOperation;
import xyz.erupt.ai_dev.proxy.DevRequirementProxy;
import xyz.erupt.ai_dev.runner.DevRunners;
import xyz.erupt.ai_dev.scope.ProjectRepoFetch;
import xyz.erupt.ai_dev.scope.DevProjectScope;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.model.EruptUser;

import java.util.Set;

/**
 * A unit of work in a project's requirement pool. It lives in the project picked in the page header
 * ({@link DevProjectScope}); its stage and progress move only through the pool's own actions. Tenants are kept
 * apart by its project: every read and write goes through a project the user is a member of.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Erupt(
        name = "Requirement",
        dataProxy = DevRequirementProxy.class,
        rowOperation = {
                @RowOperation(code = "submit", title = "Submit for Review", icon = "fa fa-paper-plane", mode = RowOperation.Mode.SINGLE,
                        operationHandler = DevRequirementOperation.class, operationParam = "submit",
                        ifExpr = "item.stage === 'DRAFT'", ifExprBehavior = RowOperation.IfExprBehavior.HIDE),
                @RowOperation(code = "approve", title = "Approve and Run", icon = "fa fa-check", mode = RowOperation.Mode.SINGLE,
                        operationHandler = DevRequirementOperation.class, operationParam = "approve", eruptClass = DevReviewForm.class,
                        ifExpr = "item.stage === 'REVIEW'", ifExprBehavior = RowOperation.IfExprBehavior.HIDE),
                @RowOperation(code = "reject", title = "Reject", icon = "fa fa-undo", mode = RowOperation.Mode.SINGLE,
                        operationHandler = DevRequirementOperation.class, operationParam = "reject", eruptClass = DevReviewForm.class,
                        ifExpr = "item.stage === 'REVIEW'", ifExprBehavior = RowOperation.IfExprBehavior.HIDE),
                @RowOperation(code = "stop", title = "Stop", icon = "fa fa-stop", mode = RowOperation.Mode.SINGLE,
                        operationHandler = DevRequirementOperation.class, operationParam = "stop",
                        ifExpr = "item.stage === 'RUNNING' || item.stage === 'VERIFYING'", ifExprBehavior = RowOperation.IfExprBehavior.HIDE),
                @RowOperation(code = "retry", title = "Retry", icon = "fa fa-repeat", mode = RowOperation.Mode.SINGLE,
                        operationHandler = DevRequirementOperation.class, operationParam = "retry", eruptClass = DevInstructionForm.class,
                        ifExpr = "item.stage === 'FAILED'", ifExprBehavior = RowOperation.IfExprBehavior.HIDE),
                @RowOperation(code = "rework", title = "Request Changes", icon = "fa fa-pencil", mode = RowOperation.Mode.SINGLE,
                        operationHandler = DevRequirementOperation.class, operationParam = "rework", eruptClass = DevInstructionForm.class,
                        ifExpr = "item.stage === 'ACCEPTANCE'", ifExprBehavior = RowOperation.IfExprBehavior.HIDE),
                @RowOperation(code = "deliver", title = "Accept and Push", icon = "fa fa-upload", mode = RowOperation.Mode.SINGLE,
                        operationHandler = DevRequirementOperation.class, operationParam = "deliver",
                        ifExpr = "item.stage === 'ACCEPTANCE'", ifExprBehavior = RowOperation.IfExprBehavior.HIDE),
                @RowOperation(code = "cleanup", title = "Clean Up Workspace", mode = RowOperation.Mode.SINGLE,
                        operationHandler = DevRequirementOperation.class, operationParam = "cleanup",
                        ifExpr = "['DRAFT', 'REVIEW', 'FAILED', 'CANCELLED'].includes(item.stage)", ifExprBehavior = RowOperation.IfExprBehavior.HIDE),
                @RowOperation(code = "priority", title = "Change Priority", mode = RowOperation.Mode.SINGLE,
                        operationHandler = DevPriorityOperation.class, eruptClass = DevPriorityForm.class),
                @RowOperation(code = "cancel", title = "Cancel", icon = "fa fa-ban", mode = RowOperation.Mode.SINGLE,
                        operationHandler = DevRequirementOperation.class, operationParam = "cancel",
                        ifExpr = "item.stage !== 'DELIVERED' && item.stage !== 'CANCELLED' && item.stage !== 'RUNNING' && item.stage !== 'VERIFYING'",
                        ifExprBehavior = RowOperation.IfExprBehavior.HIDE)
        }
)
@EruptScope(DevProjectScope.class)
@Table(name = "e_ai_dev_requirement", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "seq"}))
@Getter
@Setter
@Entity
@EruptI18n
public class DevRequirement extends MetaModelUpdateVo {

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            edit = @Edit(title = "Project", notNull = true, type = EditType.REFERENCE_TABLE,
                    readonly = @Readonly)
    )
    private DevProject project;

    // per-project sequence behind code
    @Column(nullable = false)
    private Integer seq;

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Code", width = "100px"),
            edit = @Edit(title = "Code", search = @Search, readonly = @Readonly(allowChange = false))
    )
    private String code;

    @EruptField(
            views = @View(title = "Title"),
            edit = @Edit(title = "Title", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String title;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Description", show = false),
            edit = @Edit(title = "Description", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "markdown"),
                    desc = "Background, expected behaviour and acceptance criteria: the agent works from this text")
    )
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Repositories"),
            edit = @Edit(title = "Repositories", notNull = true, type = EditType.MULTI_CHOICE,
                    multiChoiceType = @MultiChoiceType(fetchHandler = ProjectRepoFetch.class),
                    desc = "Repositories this requirement touches; each gets its own worktree")
    )
    private Set<String> repos;

    // the coding CLI that works on it, one found on the server
    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Executor"),
            edit = @Edit(title = "Executor", type = EditType.CHOICE, desc = "Coding CLIs installed on the server",
                    choiceType = @ChoiceType(fetchHandler = DevRunners.class))
    )
    private String executor;

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Model", show = false),
            edit = @Edit(title = "Model", desc = "Model id passed to the executor; blank uses its default")
    )
    private String model;

    // after the developer run, a second run of the same executor checks the change against the requirement
    @EruptField(
            views = @View(title = "QA Review", show = false),
            edit = @Edit(title = "QA Review", desc = "A second run checks the change before acceptance")
    )
    private Boolean qaReview = false;

    @Enumerated(EnumType.STRING)
    @EruptField(
            views = @View(title = "Priority"),
            edit = @Edit(title = "Priority", notNull = true, type = EditType.CHOICE, search = @Search)
    )
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @EruptField(
            views = @View(title = "Stage"),
            edit = @Edit(title = "Stage", type = EditType.CHOICE, search = @Search, readonly = @Readonly(allowChange = false))
    )
    private RequirementStage stage = RequirementStage.DRAFT;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Reviewer", column = "name"),
            edit = @Edit(title = "Reviewer", type = EditType.REFERENCE_TABLE)
    )
    private EruptUser reviewer;

    // who created it, by id: createBy holds a display name only, and notices need the account
    @ManyToOne
    @JoinColumn(name = "creator_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private EruptUser creator;

    // 0-100, reported by the agent
    @EruptField(views = @View(title = "Progress", width = "90px"))
    private Integer progress = 0;

    @EruptField(views = @View(title = "Phase"))
    private String phase;

    // the QA agent's last verdict, PASS or FAIL with its reason
    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(views = @View(title = "QA Verdict"))
    private String verdict;

    // why the last run or delivery stopped short
    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(views = @View(title = "Status Message"))
    private String statusMessage;

    // extra instruction for the next run (retry or requested changes), cleared once the run starts
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String instruction;

}
