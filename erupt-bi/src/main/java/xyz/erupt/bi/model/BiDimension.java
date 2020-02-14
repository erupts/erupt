package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DependSwitchType;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
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
            edit = @Edit(title = "维度编码")
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称")
    )
    private String title;

    @EruptField(
            views = @View(title = "维度类型"),
            edit = @Edit(
                    title = "维度类型",
                    type = EditType.DEPEND_SWITCH,
                    dependSwitchType = @DependSwitchType(
                            attr = {
                                    @DependSwitchType.Attr(value = "INPUT", label = "字符", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "NUMBER", label = "数值", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "REFERENCE", label = "SQL参照", dependEdits = {"refSql", "dependDimension"}),
                                    @DependSwitchType.Attr(value = "DATE", label = "日期", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "TIME", label = "时间", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "DATETIME", label = "日期时间", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "WEEK", label = "周", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "MONTH", label = "月", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "YEAR", label = "年", dependEdits = ""),
                            }
                    )
            )
    )
    private String type;

    @EruptField(
            views = @View(title = "默认值"),
            edit = @Edit(title = "默认值")
    )
    private String defaultVal;

    @EruptField(
            views = @View(title = "区间查询"),
            edit = @Edit(title = "区间查询")
    )
    private Boolean vague;

    @EruptField(
            views = @View(title = "是否必填"),
            edit = @Edit(title = "是否必填")
    )
    private Boolean notNull;

    @EruptField(
            views = @View(title = "显示顺序"),
            edit = @Edit(title = "显示顺序")
    )
    private Integer sort;

    @EruptField(
            views = @View(title = "依赖维度"),
            edit = @Edit(title = "依赖维度")
    )
    private String dependDimension;

    @EruptField(
            edit = @Edit(title = "参照sql", type = EditType.TEXTAREA)
    )
    private String refSql;

}

