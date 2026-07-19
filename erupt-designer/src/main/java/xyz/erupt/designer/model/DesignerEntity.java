package xyz.erupt.designer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.DragSort;
import xyz.erupt.annotation.sub_erupt.OpenWay;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.designer.handler.DesignerPublishMenu;
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
        name = "Form Designer",
        orderBy = "updateTime desc",
        dataProxy = DesignerEntityDataProxy.class,
        dragSort = @DragSort(field = "sort"),
        rowOperation = {
                @RowOperation(
                        title = "Design",
                        icon = "fa fa-paint-brush",
                        mode = RowOperation.Mode.SINGLE,
                        type = RowOperation.Type.TPL,
                        tpl = @Tpl(path = "/designer/{className}", openWay = OpenWay.ROUTER)
                ),
                @RowOperation(
                        title = "Add to Menu",
                        icon = "fa fa-send",
                        mode = RowOperation.Mode.SINGLE,
                        eruptClass = DesignerReleaseModal.class,
                        operationHandler = DesignerPublishMenu.class
                )
        }
)
@Getter
@Setter
public class DesignerEntity extends HyperModelUpdateVo {

    @Column(name = "class_name", length = 64, unique = true, nullable = false)
    @EruptField(
            views = @View(title = "Class Name"),
            edit = @Edit(title = "Class Name", notNull = true, search = @Search(operator = QueryExpression.LIKE),
                    inputType = @InputType(length = 64),
                    readonly = @Readonly(add = false)
            )
    )
    private String className;

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

    private String sort;

    @EruptField(
            views = @View(title = "Publish Time", type = xyz.erupt.annotation.sub_field.ViewType.DATE_TIME)
    )
    private Date publishTime;

    // designer JSON config, maintained by the designer page
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String config;

    private Date updateTime;

}
