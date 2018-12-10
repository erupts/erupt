package com.erupt.entity;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.sub_erupt.Tree;
import com.erupt.annotation.sub_field.Edit;
import com.erupt.model.BaseModel;

import javax.persistence.*;

/**
 * Created by liyuepeng on 11/22/18.
 */
@Entity
@Table(name = "E_MENU")
@Erupt(
        name = "菜单配置",
        tree = @Tree(id = "id", label = "name", pid = "parentMenu",icon = "icon")
)
public class EruptMenu extends BaseModel {

    private String name;

    private String sort;

    private boolean isShow;

    private String path;

    @ManyToOne
    @JoinColumn(name = "PARENT_MENU_ID")
    private EruptMenu parentMenu;

    @EruptField(
            edit = @Edit(
                    title = "图标",
                    desc = "图标请参考图标库font-awesome"
            )
    )
    private String icon;

    private String target;

    private String remark;


}
