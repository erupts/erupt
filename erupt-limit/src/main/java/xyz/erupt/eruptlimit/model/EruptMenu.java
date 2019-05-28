package xyz.erupt.eruptlimit.model;

import lombok.Data;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;

import javax.persistence.*;

/**
 * Created by liyuepeng on 11/22/18.
 */
@Entity
@Table(name = "E_MENU")
@Erupt(
        name = "菜单配置",
        sorts = "sort",
        tree = @Tree(id = "id", label = "name", pid = "parentMenu.id")
//        power = @Power(edit = false,add = false,delete = false)
)
@Data
public class EruptMenu extends BaseModel {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true)
    )
    @Column(name = "CODE", unique = true)
    private String code = "233333333";

    @Column(name = "NAME")
    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(
                    title = "名称",
                    notNull = true
            )
    )
    private String name = "2344524";

    @Column(name = "PATH")
    @EruptField(
            edit = @Edit(
                    title = "地址",
                    inputType = @InputType(
                            prefix = {
                                    @VL(value = "/build/table/", label = "TABLE"),
                                    @VL(value = "/build/tree/", label = "TREE"),
                                    @VL(value = "/build/report/", label = "REPORT"),
                                    @VL(value = "/page/", label = "PAGE"),
                                    @VL(value = "/", label = "/"),
                            }
                    )
            )
    )
    private String path;

    @Column(name = "STATUS")
    @EruptField(
            edit = @Edit(
                    notNull = true,
                    title = "菜单状态",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            vl = {
                                    @VL(value = "1", label = "启用"),
                                    @VL(value = "2", label = "隐藏"),
                                    @VL(value = "3", label = "禁用"),
                            }
                    )
            )
    )
    private Integer status = 1;

    @Column(name = "SORT")
    @EruptField(
            edit = @Edit(
                    title = "顺序"
            )
    )
    private Integer sort;


    @Column(name = "ICON")
    @EruptField(
            edit = @Edit(
                    title = "图标",
                    desc = "请参考图标库font-awesome（仅会在最父级节点中展示）"
            )
    )
    private String icon;


    @ManyToOne
    @JoinColumn(name = "PARENT_MENU_ID")
    @EruptField(
            edit = @Edit(
                    title = "上级菜单",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "id")
            )
    )
    private EruptMenu parentMenu;


    @Column(name = "TARGET")
    @EruptField(
            edit = @Edit(
                    title = "打开方式",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = "0", label = "_target"),
                            @VL(value = "1", label = "_self")
                    })
            )
    )
    private Integer target;

    @Column(name = "REMARK")
    @EruptField(
            edit = @Edit(
                    title = "功能描述",
                    type = EditType.HTML_EDIT
//                    inputType = @InputType(type = InputEnum.TEXTAREA)
            )
    )
    private String remark;

}
