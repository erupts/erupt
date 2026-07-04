package xyz.erupt.upms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.upms.handler.RoleMenuFilter;
import xyz.erupt.upms.helper.HyperModelUpdateVo;
import xyz.erupt.upms.model.data_proxy.EruptRoleDataProxy;

import java.util.Set;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_role")
@Erupt(name = "Role Management", dataProxy = EruptRoleDataProxy.class, orderBy = "EruptRole.sort asc")
@EruptI18n
@Getter
@Setter
public class EruptRole extends HyperModelUpdateVo {

    @Column(length = AnnotationConst.CODE_LENGTH, unique = true)
    @EruptField(
            views = @View(title = "code"),
            edit = @Edit(title = "code", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String code;

    @EruptField(
            views = @View(title = "name"),
            edit = @Edit(title = "name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Display Order", sortable = true),
            edit = @Edit(title = "Display Order")
    )
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
    @ManyToMany(fetch = FetchType.EAGER)
    @EruptField(
            views = @View(title = "Contain User"),
            edit = @Edit(
                    title = "Contain User",
                    type = EditType.TAB_TABLE_REFER
            )
    )
    private Set<EruptUserByRoleView> users;


}
