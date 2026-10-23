package xyz.erupt.ai_dev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.TenantId;
import xyz.erupt.ai_dev.constant.RepoStatus;
import xyz.erupt.ai_dev.scope.RemoteBranchFetch;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.AutoCompleteType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.jpa.model.BaseModel;

import java.time.LocalDateTime;

/**
 * One git repository of a project. The server keeps a bare clone of it; requirement worktrees are
 * checked out from that clone into a directory named after {@link #alias}.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Erupt(name = "Repository")
@Table(name = "e_ai_dev_project_repo")
@Getter
@Setter
@Entity
@EruptI18n
public class DevProjectRepo extends BaseModel {

    // owner tenant, UpmsTenant.PLATFORM for the platform: stamped from the writer's session and
    // applied by Hibernate to every query, so one tenant never reads another's rows
    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    @EruptField(
            views = @View(title = "Alias"),
            edit = @Edit(title = "Alias", notNull = true, inputType = @InputType(regex = DevProjectRepo.ALIAS_REGEX),
                    desc = "Directory name inside the workspace, e.g. web or api: lower case letters, digits and -")
    )
    private String alias;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Git URL"),
            edit = @Edit(title = "Git URL", notNull = true,
                    desc = "https, ssh or git@host:org/repo.git; never put credentials in the URL")
    )
    private String url;

    @EruptField(
            views = @View(title = "Base Branch"),
            edit = @Edit(title = "Base Branch", type = EditType.AUTO_COMPLETE,
                    desc = "Worktrees start from the latest origin of this branch; blank takes the remote's default branch",
                    // the remote's branches as soon as the field is focused
                    autoCompleteType = @AutoCompleteType(handler = RemoteBranchFetch.class, triggerLength = 0))
    )
    private String baseBranch;

    @EruptField(
            views = @View(title = "Target Branch"),
            edit = @Edit(title = "Target Branch", type = EditType.AUTO_COMPLETE,
                    desc = "Branch a delivery is pushed to after rebasing; blank pushes to a branch of its own, req/<code>",
                    autoCompleteType = @AutoCompleteType(handler = RemoteBranchFetch.class, triggerLength = 0))
    )
    private String targetBranch;

    @EruptField(
            views = @View(title = "Credential"),
            edit = @Edit(title = "Credential",
                    desc = "Key of an entry under erupt.ai-dev.credentials in the server configuration; blank for public repositories")
    )
    private String credential;

    @Enumerated(EnumType.STRING)
    @EruptField(views = @View(title = "Sync Status"))
    private RepoStatus status = RepoStatus.PENDING;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(views = @View(title = "Sync Message"))
    private String statusMessage;

    @EruptField(views = @View(title = "Synced At"))
    private LocalDateTime syncedAt;

    public static final String ALIAS_REGEX = "^[a-z0-9][a-z0-9-]{0,31}$";

}
