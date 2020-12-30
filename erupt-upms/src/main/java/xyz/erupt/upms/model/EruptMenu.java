package xyz.erupt.upms.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.upms.model.base.HyperModel;
import xyz.erupt.upms.service.EruptMenuService;
import xyz.erupt.upms.util.MenuTool;

import javax.persistence.*;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_menu", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(
        name = "菜单配置",
        orderBy = "EruptMenu.sort asc",
        tree = @Tree(pid = "parentMenu.id"),
        dataProxy = EruptMenuService.class
)
@Getter
@Setter
public class EruptMenu extends HyperModel implements ChoiceFetchHandler {

    public static final String OPEN = "1";

    public static final String HIDE = "2";

    public static final String DISABLE = "3";

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(
                    title = "名称",
                    notNull = true
            )
    )
    private String name;

    @EruptField(
            edit = @Edit(
                    notNull = true,
                    title = "状态",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            vl = {
                                    @VL(value = OPEN, label = "启用"),
                                    @VL(value = HIDE, label = "隐藏"),
                                    @VL(value = DISABLE, label = "禁用"),
                            }
                    )
            )
    )
    private Integer status;

    @EruptField(
            edit = @Edit(
                    title = "菜单类型",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = EruptMenu.class)
            )
    )
    private String type;

    @EruptField(
            edit = @Edit(
                    title = "类型值"
            )
    )
    private String value;


    @EruptField(
            edit = @Edit(
                    title = "顺序"
            )
    )
    private Integer sort = 0;


    @EruptField(
            edit = @Edit(
                    title = "图标",
                    desc = "请参考图标库font-awesome"
            )
    )
    private String icon;

    @ManyToOne
    @EruptField(
            edit = @Edit(
                    title = "上级菜单",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentMenu.id")
            )
    )
    private EruptMenu parentMenu;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(
                    title = "菜单参数",
                    type = EditType.TEXTAREA
            )
    )
    private String param;

    public EruptMenu(String code, String name, String type, String value, Integer status, Integer sort, String icon, EruptMenu parentMenu) {
        this.code = code;
        this.name = name;
        this.status = status;
        this.type = type;
        this.value = value;
        this.sort = sort;
        this.icon = icon;
        this.parentMenu = parentMenu;
    }

    public EruptMenu() {
    }

    @Override
    public List<VLModel> fetch(String[] params) {
        return MenuTool.getMenuTypes();
    }
}
