package xyz.erupt.eruptlimit.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import lombok.Data;
import xyz.erupt.annotation.sub_field.sub_edit.*;

import javax.persistence.*;

/**
 * Created by liyuepeng on 11/22/18.
 */
@Entity
@Table(name = "E_MENU")
@Erupt(
        name = "菜单配置",
        tree = @Tree(id = "id", label = "name", pid = "parentMenu.id")
)
@Data
public class EruptMenu extends BaseModel {

    @Column(name = "NAME")
    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(
                    title = "名称",
                    notNull = true
            )
    )
    private String name;

    @Column(name = "SORT")
    @EruptField(
            edit = @Edit(
                    title = "顺序"
            )
    )
    private Integer sort;


    @Column(name = "IS_SHOW")
    @EruptField(
            edit = @Edit(
                    title = "是否展示",
                    type = EditType.BOOLEAN,
                    boolType = @BoolType(trueText = "展示", falseText = "隐藏", defaultValue = true)
            )
    )
    private boolean isShow;

    @Transient
    @EruptField(
            edit = @Edit(
                    title = "分组1",
                    type = EditType.DIVIDE
            )
    )
    private String group1;

    @Column(name = "PATH")
    @EruptField(
            edit = @Edit(
                    title = "地址"
            )
    )
    private String path;

    @ManyToOne
    @JoinColumn(name = "PARENT_MENU_ID")
    @EruptField(
            edit = @Edit(
                    title = "上级菜单",
                    type = EditType.REFERENCE,
                    referenceType = @ReferenceType(id = "id", label = "name")
            )
    )
    private EruptMenu parentMenu;

    @Column(name = "ICON")
    @EruptField(
            edit = @Edit(
                    title = "图标",
                    desc = "图标请参考图标库font-awesome"
            )
    )
    private String icon;

    @Column(name = "TARGET")
    @EruptField(
            edit = @Edit(
                    title = "打开方式",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = 0, label = "_target"),
                            @VL(value = 1, label = "_self")
                    }, type = ChoiceEnum.RADIO)
            )
    )
    private Integer target;

    @Column(name = "REMARK")
    @EruptField(
            edit = @Edit(
                    title = "描述",
                    inputType = @InputType(type = InputEnum.TEXTAREA)
            )
    )
    private String remark;


}
