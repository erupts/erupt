package xyz.erupt.eruptlimit.model;

import lombok.Data;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.TabEnum;
import xyz.erupt.annotation.sub_field.sub_edit.TabType;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by liyuepeng on 11/22/18.
 */
@Entity
@Table(name = "E_ROLE")
@Erupt(name = "用户角色")
@Data
public class EruptRole extends BaseModel {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true)
    )
    @Column(name = "CODE")
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    @Column(name = "NAME")
    private String name;

    @Column(name = "STATUS")
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
                    type = EditType.TAB
            )
    )
    private Set<EruptMenu> menus;

    @ManyToMany(mappedBy = "roles")
    @EruptField(
            edit = @Edit(
                    title = "包含用户",
                    type = EditType.TAB
            )
    )
    private Set<EruptUser> users;
}
