package xyz.erupt.upms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.TenantId;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.helper.UpmsTenant;

/**
 * @author YuePeng
 * date 2020-04-08
 */
@Entity
@Table(name = "e_upms_org", uniqueConstraints = @UniqueConstraint(name = "uk_upms_org_code", columnNames = {"tenantId", "code"}))
@Erupt(
        name = "Org Management",
        tree = @Tree(pid = "parentOrg.id", expandLevel = 5),
        orderBy = "EruptOrg.sort asc"
)
@EruptI18n
@Getter
@Setter
@NoArgsConstructor
public class EruptOrg extends BaseModel {

    // Owner tenant, UpmsTenant.PLATFORM for the platform, restricted and stamped by Hibernate
    // (see UpmsTenantIdentifierResolver)
    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Org Code", sortable = true),
            edit = @Edit(title = "Org Code", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String code;

    @EruptField(
            views = @View(title = "Org Name", sortable = true),
            edit = @Edit(title = "Org Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @ManyToOne
    @EruptField(
            edit = @Edit(
                    title = "Parent Org",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentOrg.id", expandLevel = 3)
            )
    )
    private EruptOrg parentOrg;

    @EruptField(
            edit = @Edit(
                    title = "Display Order"
            )
    )
    private Integer sort;


}
