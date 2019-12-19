package xyz.erupt.report.model;

import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;

/**
 * Created by liyuepeng on 2019-08-26.
 */
public class BiColumn {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码")
    )
    private String code;

    private String name;

    @EruptField(
            edit = @Edit(title = "数据类型")
    )
    private String type;

    private Integer sort;

    private Boolean export;

    private Boolean sortable;
}
