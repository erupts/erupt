package xyz.erupt.report.model;

import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DependSwitchType;
import xyz.erupt.core.model.BaseModel;

/**
 * Created by liyuepeng on 2019-08-26.
 */
public class BiDimension extends BaseModel {

    @EruptField(
            edit = @Edit(title = "维度编码")
    )
    private String code;

    @EruptField(
            edit = @Edit(title = "映射值")
    )
    private String name;

    @EruptField(
            edit = @Edit(
                    title = "维度类型",
                    type = EditType.DEPEND_SWITCH,
                    dependSwitchType = @DependSwitchType(
                            attr = {
                                    @DependSwitchType.Attr(value = "input", label = "字符串", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "number", label = "数值", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "reference", label = "参照",
                                            dependEdits = {"refSql", "refDataSource"}),
                                    @DependSwitchType.Attr(value = "date", label = "日期", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "dateTime", label = "日期时间", dependEdits = ""),
                            }
                    )
            )
    )
    private String type;

    @EruptField(
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
            edit = @Edit(title = "参照sql")
    )
    private String refSql;

    @EruptField(
            edit = @Edit(title = "参照数据源")
    )
    private BiDataSource refDataSource;

}
