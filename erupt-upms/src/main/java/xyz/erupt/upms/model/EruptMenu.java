package xyz.erupt.upms.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.upms.enums.MenuLimitEnum;
import xyz.erupt.upms.model.base.HyperModel;
import xyz.erupt.upms.service.EruptMenuService;

import javax.persistence.*;
import java.util.Date;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_menu", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(
        name = "菜单管理",
        orderBy = "EruptMenu.sort asc",
        tree = @Tree(pid = "parentMenu.id"),
        dataProxy = EruptMenuService.class
)
@EruptI18n
@Getter
@Setter
public class EruptMenu extends HyperModel {

    public static final String CODE = "code";

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
                    choiceType = @ChoiceType(fetchHandler = MenuStatus.ChoiceFetch.class)
            )
    )
    private Integer status;

    @ManyToOne
    @EruptField(
            edit = @Edit(
                    title = "上级菜单",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentMenu.id")
            )
    )
    private EruptMenu parentMenu;

    @EruptField(
            edit = @Edit(
                    title = "菜单类型",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = MenuTypeEnum.ChoiceFetch.class)
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

    @EruptField(
            edit = @Edit(
                    title = "权限",
                    type = EditType.TAGS,
                    showBy = @ShowBy(dependField = "type", expr = "value=='tree'||value=='table'"),
                    tagsType = @TagsType(fetchHandler = MenuLimitEnum.MenuLimitFetch.class, allowExtension = false)
            )
    )
    private String powerOff;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(
                    title = "自定义参数",
                    desc = "json格式，通过上下文获取，根据业务需求解析",
                    type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "json")
            )
    )
    private String param;

    public EruptMenu() {
    }

    public EruptMenu(String code, String name, String type, String value, Integer status, Integer sort, String icon, EruptMenu parentMenu) {
        this.code = code;
        this.name = name;
        this.status = status;
        this.type = type;
        this.value = value;
        this.sort = sort;
        this.icon = icon;
        this.parentMenu = parentMenu;
        this.setCreateTime(new Date());
    }

    public static EruptMenu fromMetaMenu(MetaMenu metaMenu) {
        if (null == metaMenu) return null;
        EruptMenu eruptMenu =  new EruptMenu(metaMenu.getCode(),
                metaMenu.getName(),
                null == metaMenu.getType() ? null : metaMenu.getType(),
                metaMenu.getValue(),
                null == metaMenu.getStatus() ? null : metaMenu.getStatus().getValue(),
                metaMenu.getSort(),
                metaMenu.getIcon(),
                fromMetaMenu(metaMenu.getParentMenu()));
        eruptMenu.setId(metaMenu.getId());
        return eruptMenu;
    }

}
