package xyz.erupt.report.model;

import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;

import javax.persistence.OneToMany;

/**
 * Created by liyuepeng on 2019-08-26.
 */
public class BiReport {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true, search = @Search(vague = true, value = true))
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true, value = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "数据源"),
            edit = @Edit(title = "数据源", type = EditType.REFERENCE_TREE, search = @Search(value = true))
    )
    private BiDataSource dataSource;

    @EruptField(
            edit = @Edit(title = "handler",placeHolder = "类路径", desc = "需实现xyz.erupt.report.fun.ReportHandler接口")
    )
    private String handler;

    @EruptField(
            views = @View(title = "是否可导出"),
            edit = @Edit(title = "是否可导出", search = @Search(true))
    )
    private Boolean export;

    @EruptField(
            edit = @Edit(title = "sql", type = EditType.TEXTAREA)
    )
    private String sql;

    @EruptField(
            edit = @Edit(title = "总条数sql", type = EditType.TEXTAREA)
    )
    private String countSql;

    @EruptField(
            edit = @Edit(title = "合计sql", type = EditType.TEXTAREA)
    )
    private String totalSql;

    @OneToMany
    private BiDimension biDimension;

    @OneToMany
    private BiColumn biColumn;

}
