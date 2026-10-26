package xyz.erupt.ai_dev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.TenantId;
import xyz.erupt.ai_dev.proxy.DevProjectProxy;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A software project: the repositories it spans, the people and agents working on it, and the
 * AGENTS.md every agent run reads. Requirements, worktrees and agent sessions all live inside one.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Erupt(
        name = "Dev Project",
        dataProxy = DevProjectProxy.class,
        rowOperation = {
                @RowOperation(code = "sync", icon = "fa fa-refresh", title = "Sync Repositories",
                        mode = RowOperation.Mode.SINGLE, operationHandler = DevProjectProxy.class, operationParam = "sync"),
                @RowOperation(code = "cleanup", icon = "fa fa-eraser", title = "Clean Up Leftover Workspaces",
                        mode = RowOperation.Mode.SINGLE, operationHandler = DevProjectProxy.class, operationParam = "cleanup",
                        tip = "Removes the workspaces of delivered and cancelled requirements, and of requirements that no longer exist")
        }
)
@Table(name = "e_ai_dev_project", uniqueConstraints = @UniqueConstraint(name = "uk_ai_dev_project_code", columnNames = {"tenantId", "code"}))
@Getter
@Setter
@Entity
@EruptI18n
public class DevProject extends MetaModelUpdateVo {

    // owner tenant, UpmsTenant.PLATFORM for the platform: stamped from the writer's session and
    // applied by Hibernate to every query, so one tenant never reads another's rows
    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    @EruptField(
            views = @View(title = "Code"),
            edit = @Edit(title = "Code", notNull = true, search = @Search(operator = QueryExpression.LIKE),
                    inputType = @InputType(regex = DevProject.CODE_REGEX),
                    readonly = @Readonly(add = false, allowChange = false),
                    desc = "Unique identifier, also the directory name on the server: lower case letters, digits and -; cannot be changed later")
    )
    private String code;

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    @EruptField(
            views = @View(title = "Repositories", show = false),
            edit = @Edit(title = "Repositories", type = EditType.TAB_TABLE_ADD,
                    desc = "Each repository is cloned once on the server; a requirement picks the ones it touches")
    )
    private Set<DevProjectRepo> repos = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    @EruptField(
            views = @View(title = "Members", show = false),
            edit = @Edit(title = "Members", type = EditType.TAB_TABLE_ADD,
                    desc = "Only members see the project; owners and maintainers manage it")
    )
    private Set<DevProjectMember> members = new LinkedHashSet<>();


    // four eyes by default: whoever writes a requirement does not approve it
    @EruptField(
            views = @View(title = "Self Review", show = false),
            edit = @Edit(title = "Self Review", desc = "Let the creator of a requirement approve or reject it")
    )
    private Boolean selfReview = false;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "AGENTS.md", show = false),
            edit = @Edit(title = "AGENTS.md", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "markdown"),
                    desc = "Project instructions written into the workspace root of every agent run, next to the repositories")
    )
    private String agentsMd;

    // requirement workspaces of the project still on the server, counted when listed
    @Transient
    @EruptField(views = @View(title = "Active Workspaces", width = "120px"))
    private Integer activeWorkspaces;

    // who created it, by id: the project list shows a user only their own, super admins every one
    @ManyToOne
    @JoinColumn(name = "creator_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private EruptUserVo creator;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

    public static final String CODE_REGEX = "^[a-z0-9][a-z0-9-]{0,62}$";

}
