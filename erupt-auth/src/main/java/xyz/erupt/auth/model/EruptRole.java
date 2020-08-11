package xyz.erupt.auth.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.auth.model.base.BaseModel;

import javax.persistence.*;
import java.util.Set;

/**
 * @author liyuepeng
 * @date 2018-11-22.
 */
@Entity
@Table(name = "E_ROLE", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
@Erupt(name = "用户角色")
@Getter
@Setter
public class EruptRole extends BaseModel {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "状态"),
            edit = @Edit(
                    title = "状态",
                    type = EditType.BOOLEAN,
                    boolType = @BoolType(trueText = "启用", falseText = "禁用")
            )
    )
    private Boolean status;

    @ManyToMany
    @JoinTable(
            name = "E_ROLE_MENU",
            joinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "MENU_ID", referencedColumnName = "ID"))
    @EruptField(
            edit = @Edit(
                    title = "菜单",
                    type = EditType.TAB_TREE
            )
    )
    private Set<EruptMenu> menus;

    @ManyToMany
    @JoinTable(
            name = "E_USER_ROLE",
            joinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"))
    @EruptField(
            edit = @Edit(
                    title = "包含用户",
                    type = EditType.TAB_TABLE_REFER
            )
    )
    private Set<EruptUser> users;
}
