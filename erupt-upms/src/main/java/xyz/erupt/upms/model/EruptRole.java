package xyz.erupt.upms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.TenantId;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.DragSort;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.upms.handler.RoleMenuFilter;
import xyz.erupt.upms.helper.HyperModelUpdateVo;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.model.data_proxy.EruptRoleDataProxy;

import java.util.Set;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_role", uniqueConstraints = @UniqueConstraint(name = "uk_upms_role_code", columnNames = {"tenantId", "code"}))
@Erupt(name = "Role Management", dataProxy = EruptRoleDataProxy.class,
        dragSort = @DragSort(field = "sort"),
        orderBy = "EruptRole.sort asc")
@EruptI18n
@Getter
@Setter
public class EruptRole extends HyperModelUpdateVo {

    // Owner tenant, UpmsTenant.PLATFORM for the platform, restricted and stamped by Hibernate
    // (see UpmsTenantIdentifierResolver)
    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "code"),
            // every permission binding and @Match expression keys off this code
            edit = @Edit(title = "code", notNull = true, cellEdit = false,
                    search = @Search(operator = QueryExpression.LIKE))
    )
    private String code;

    @EruptField(
            views = @View(title = "name"),
            edit = @Edit(title = "name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField
    private Integer sort;

    @EruptField(
            views = @View(title = "status", sortable = true),
            edit = @Edit(
                    title = "status",
                    type = EditType.BOOLEAN,
                    notNull = true,
                    search = @Search,
                    boolType = @BoolType(trueText = "Enable", falseText = "Disable")
            )
    )
    private Boolean status = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "e_upms_role_menu",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id", referencedColumnName = "id"),
            foreignKey = @ForeignKey(name = "fk_role_menu_role"),
            inverseForeignKey = @ForeignKey(name = "fk_role_menu_menu"))
    @EruptField(
            views = @View(title = "Menu Permission"),
            edit = @Edit(
                    filter = @Filter(conditionHandler = RoleMenuFilter.class),
                    title = "Menu Permission",
                    type = EditType.TAB_TREE
            )
    )
    private Set<EruptMenu> menus;

    @JoinTable(name = "e_upms_user_role",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            foreignKey = @ForeignKey(name = "fk_user_role_role"),
            inverseForeignKey = @ForeignKey(name = "fk_user_role_user"))
    // Lazy on purpose: EruptUser.roles is eager, and an eager users set here would pull every member
    // of every role of the user (with their org and post) into each current-user lookup
    @ManyToMany(fetch = FetchType.LAZY)
    @EruptField(
            views = @View(title = "Contain User"),
            edit = @Edit(
                    title = "Contain User",
                    type = EditType.TAB_TABLE_REFER
            )
    )
    private Set<EruptUserByRoleView> users;


}
