package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.DependSwitchType;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
@Entity
@Table(name = "E_BI_DIMENSION")
@Erupt(name = "查询维度")
@Getter
@Setter
public class BiDimension extends BaseModel {

    @EruptField(
            views = @View(title = "维度编码"),
            edit = @Edit(title = "维度编码", notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String title;

    @EruptField(
            views = @View(title = "显示顺序", sortable = true),
            edit = @Edit(title = "显示顺序")
    )
    private Integer sort;

    @EruptField(
            views = @View(title = "是否必填"),
            edit = @Edit(title = "是否必填", boolType = @BoolType(defaultValue = false))
    )
    private Boolean notNull;

    @EruptField(
            views = @View(title = "维度类型"),
            edit = @Edit(
                    title = "维度类型",
                    notNull = true,
                    type = EditType.DEPEND_SWITCH,
                    dependSwitchType = @DependSwitchType(
                            view = DependSwitchType.View.RADIO,
                            attr = {
                                    @DependSwitchType.Attr(value = "INPUT", label = "文本", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "NUMBER", label = "数值", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "NUMBER_RANGE", label = "数值区间", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "REFERENCE", label = "SQL参照", dependEdits = {"refSql", "biDimensionReference"}),
                                    @DependSwitchType.Attr(value = "DATE", label = "日期", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "DATE_RANGE", label = "日期区间", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "DATETIME", label = "日期时间", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "DATETIME_RANGE", label = "日期时间区间", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "TIME", label = "时间", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "WEEK", label = "周", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "MONTH", label = "月", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "YEAR", label = "年", dependEdits = ""),
                            }
                    )
            )
    )
    private String type;

    @ManyToOne
    @EruptField(
            views = @View(title = "参照维度", column = "name"),
            edit = @Edit(title = "参照维度", type = EditType.REFERENCE_TABLE)
    )
    private BiDimensionReference biDimensionReference;


}

