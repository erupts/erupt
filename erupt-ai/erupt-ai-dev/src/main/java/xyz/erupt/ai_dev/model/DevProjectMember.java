package xyz.erupt.ai_dev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.TenantId;
import xyz.erupt.ai_dev.constant.MemberRole;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Layout;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUser;

/**
 * @author YuePeng
 * date 2026/10/21
 */
@Erupt(name = "Project Member",layout = @Layout(formSize = Layout.FormSize.FULL_LINE))
@Table(name = "e_ai_dev_project_member")
@Getter
@Setter
@Entity
@EruptI18n
public class DevProjectMember extends BaseModel {

    // owner tenant, UpmsTenant.PLATFORM for the platform: stamped from the writer's session and
    // applied by Hibernate to every query, so one tenant never reads another's rows
    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "User", column = "name"),
            edit = @Edit(title = "User", notNull = true, type = EditType.REFERENCE_TABLE)
    )
    private EruptUser user;

    @Enumerated(EnumType.STRING)
    @EruptField(
            views = @View(title = "Role"),
            edit = @Edit(title = "Role", notNull = true, type = EditType.CHOICE)
    )
    private MemberRole role = MemberRole.MEMBER;

}
