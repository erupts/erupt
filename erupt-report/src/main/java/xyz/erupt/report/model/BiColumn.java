package xyz.erupt.report.model;

import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.model.BaseModel;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
public class BiColumn extends BaseModel {

    @EruptField(
            views = @View(title = "列名"),
            edit = @Edit(title = "列名")
    )
    private String col;

    @EruptField(
            views = @View(title = "映射名"),
            edit = @Edit(title = "映射名")
    )
    private String mapping;

    @EruptField(
            views = @View(title = "显示顺序"),
            edit = @Edit(title = "显示顺序")
    )
    private Integer sort;

    @EruptField(
            views = @View(title = "导出"),
            edit = @Edit(title = "导出")
    )
    private Boolean export;

    @EruptField(
            views = @View(title = "排序"),
            edit = @Edit(title = "排序")
    )
    private Boolean sortable;
}
