package xyz.erupt.upms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.TenantId;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.helper.UpmsTenant;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_user")
@Erupt(
        name = "User Config"
)
@EruptI18n
@Getter
@Setter
public class EruptUserByRoleView extends BaseModel {

    // same table as EruptUser: the tenant restriction has to apply to this view as well
    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    @EruptField(
            views = @View(title = "Account", sortable = true),
            edit = @Edit(title = "Account", desc = "Login account", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String account;

    @EruptField(
            views = @View(title = "Full Name", sortable = true),
            edit = @Edit(title = "Full Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Account Status", sortable = true),
            edit = @Edit(
                    title = "Account Status",
                    search = @Search,
                    type = EditType.BOOLEAN,
                    notNull = true,
                    boolType = @BoolType(
                            trueText = "Activate",
                            falseText = "Locked"
                    )
            )
    )
    private Boolean status = true;

    @EruptField(
            views = @View(title = "Admin User", sortable = true),
            edit = @Edit(
                    title = "Admin User", notNull = true
            )
    )
    private Boolean isAdmin = false;

    @ManyToOne
    @EruptField(
            views = @View(title = "Org", column = "name"),
            edit = @Edit(title = "Org", type = EditType.REFERENCE_TREE, referenceTreeType = @ReferenceTreeType(pid = "parentOrg.id"))
    )
    private EruptOrg eruptOrg;

    @ManyToOne
    @EruptField(
            views = @View(title = "Post", column = "name"),
            edit = @Edit(title = "Post", type = EditType.REFERENCE_TREE)
    )
    private EruptPost eruptPost;

}
