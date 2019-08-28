package xyz.erupt.report.model;

import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.DependSwitchType;

/**
 * Created by liyuepeng on 2019-08-26.
 */
public class BiDimension {

    private String code;

    private String name;

    @EruptField(
            edit = @Edit(
                    title = "数据类型",
                    type = EditType.DEPEND_SWITCH,
                    dependSwitchType = @DependSwitchType(
                            attr = {
                                    @DependSwitchType.Attr(value = "input", label = "字符串", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "number", label = "数值", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "reference", label = "参照", dependEdits = ""),
                                    @DependSwitchType.Attr(value = "date", label = "日期", dependEdits = "vague"),
                                    @DependSwitchType.Attr(value = "dateTime", label = "日期时间", dependEdits = "vague"),
                            }
                    )
            )
    )
    private String type;

    private Boolean vague;

    private Boolean notNull;

    private Integer sort;

    private String param;

    private BiDataSource biDataSource;

}
