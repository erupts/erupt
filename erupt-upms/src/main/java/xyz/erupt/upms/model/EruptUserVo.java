package xyz.erupt.upms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import xyz.erupt.annotation.sub_field.View;
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
        name = "User Management"
)
@EruptI18n
@Getter
@Setter
public class EruptUserVo extends BaseModel {

    // same table as EruptUser: the tenant restriction has to apply to this view as well
    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    private String avatar;

    @EruptField(
            views = @View(title = "Full Name", sortable = true),
            edit = @Edit(title = "Full Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    private Boolean status;

    public EruptUserVo() {
    }

    public EruptUserVo(Long id) {
        this.setId(id);
    }

}
