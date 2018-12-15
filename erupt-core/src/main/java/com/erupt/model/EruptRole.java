package com.erupt.model;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.sub_field.Edit;
import com.erupt.annotation.sub_field.EditType;
import com.erupt.annotation.sub_field.View;
import com.erupt.annotation.sub_field.sub_edit.*;
import lombok.Data;

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
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    @Column(name = "NAME")
    private String name;

    @EruptField(
            views = @View(title = "描述"),
            edit = @Edit(title = "描述")
    )
    @Column(name = "REMARK")
    private String remark;

    @Column(name = "STATUS")
    @EruptField(
            edit = @Edit(
                    title = "状态",
                    type = EditType.BOOLEAN,
                    boolType = @BoolType(trueText = "启用", falseText = "禁用", defaultValue = true)
            )
    )
    private boolean status;

    @ManyToMany
    @JoinTable(
            name = "E_ROLE_MENU",
            joinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "MENU_ID", referencedColumnName = "ID"))
    @EruptField(
            edit = @Edit(
                    title = "菜单",
                    type = EditType.TAB,
                    tabType = @TabType
            )
    )
    private Set<EruptMenu> menus;

    @ManyToMany(mappedBy = "roles")
    @EruptField(
            edit = @Edit(
                    title = "包含用户",
                    type = EditType.TAB,
                    tabType = @TabType
            )
    )
    private Set<EruptUser> users;
}
