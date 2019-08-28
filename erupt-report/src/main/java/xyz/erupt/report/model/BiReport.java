package xyz.erupt.report.model;

import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;

import javax.persistence.OneToMany;

/**
 * Created by liyuepeng on 2019-08-26.
 */
public class BiReport {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码")
    )
    private String code;

    @EruptField(
            views = @View(title = "报表名称"),
            edit = @Edit(title = "报表名称")
    )
    private String name;

    @EruptField(
            views = @View(title = "数据源"),
            edit = @Edit(title = "数据源")
    )
    private BiDataSource dataSource;

    @EruptField(
            views = @View(title = "handler"),
            edit = @Edit(title = "handler", desc = "该类须实现xyz.erupt.report.fun.ReportHandler接口")
    )
    private String handler;

    @EruptField(
            views = @View(title = "导出"),
            edit = @Edit(title = "导出")
    )
    private Boolean export;

    @EruptField(
            views = @View(title = "sql"),
            edit = @Edit(title = "sql")
    )
    private String sql;

    @EruptField(
            views = @View(title = "总条数sql"),
            edit = @Edit(title = "总条数sql")
    )
    private String countSql;

    @EruptField(
            views = @View(title = "合计sql"),
            edit = @Edit(title = "合计sql")
    )
    private String totalSql;

    @OneToMany
    private BiDimension biDimension;

    @OneToMany
    private BiColumn biColumn;

}
