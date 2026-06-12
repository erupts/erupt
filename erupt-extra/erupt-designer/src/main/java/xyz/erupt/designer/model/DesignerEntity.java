package xyz.erupt.designer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.OpenWay;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.designer.proxy.DesignerEntityDataProxy;
import xyz.erupt.upms.helper.HyperModelUpdateVo;

import java.util.Date;

/**
 * Designed form models, managed like any other erupt table;
 * the row operation jumps into the visual designer (see CubeDashboard → Puzzle).
 *
 * @author YuePeng
 * date 2026-06-12
 */
@Entity
@Table(name = "e_designer")
@Erupt(
        name = "表单设计",
        orderBy = "updateTime desc",
        dataProxy = DesignerEntityDataProxy.class,
        rowOperation = @RowOperation(
                title = "设计",
                icon = "fa fa-paint-brush",
                mode = RowOperation.Mode.SINGLE,
                type = RowOperation.Type.TPL,
                tpl = @Tpl(path = "/designer/{className}", openWay = OpenWay.ROUTER)
        )
)
@Getter
@Setter
public class DesignerEntity extends HyperModelUpdateVo {

    @Column(name = "class_name", length = 64, unique = true, nullable = false)
    @EruptField(
            views = @View(title = "类名"),
            edit = @Edit(title = "类名", notNull = true, search = @Search,
                    inputType = @InputType(length = 64),
                    readonly = @Readonly(add = false)
            )
    )
    private String className;

    @EruptField(
            views = @View(title = "功能名称"),
            edit = @Edit(title = "功能名称", notNull = true, search = @Search)
    )
    private String name;

    @EruptField(
            views = @View(title = "备注"),
            edit = @Edit(title = "备注", type = EditType.TEXTAREA)
    )
    private String remark;

    @EruptField(
            views = @View(title = "发布时间", type = xyz.erupt.annotation.sub_field.ViewType.DATE_TIME)
    )
    private Date publishTime;

    // 设计器 JSON 配置，由设计器页面维护
    @Column(length = AnnotationConst.REMARK_LENGTH)
    private String config;

    private Date updateTime;

}
