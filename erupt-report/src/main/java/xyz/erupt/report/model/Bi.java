package xyz.erupt.report.model;

import lombok.Getter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.*;
import java.util.Set;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
@Entity
@Table(name = "E_BI")
@Erupt(name = "报表配置")
@Getter
public class Bi extends BaseModel {

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

    @ManyToOne
    @JoinColumn(name = "DATASOURCE_ID")
    @EruptField(
            views = @View(title = "数据源", column = "name"),
            edit = @Edit(title = "数据源", notNull = true, type = EditType.REFERENCE_TREE, search = @Search(value = true))
    )
    private BiDataSource dataSource;

    @EruptField(
            edit = @Edit(title = "handler", placeHolder = "处理类", desc = "需实现xyz.erupt.report.fun.ReportHandler接口")
    )
    private String handler;

    @EruptField(
            views = @View(title = "是否可导出"),
            edit = @Edit(title = "是否可导出", search = @Search(true))
    )
    private Boolean export;

    @Lob
    @EruptField(
            edit = @Edit(title = "sql", type = EditType.TEXTAREA, notNull = true)
    )
    private String sql;

    @Lob
    @EruptField(
            edit = @Edit(title = "总条数sql", type = EditType.TEXTAREA)
    )
    private String countSql;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "BI_ID")
    @EruptField(
            edit = @Edit(title = "查询维度", type = EditType.TAB_TABLE_ADD)
    )
    private Set<BiDimension> biDimension;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "BI_ID")
    @EruptField(
            edit = @Edit(title = "数据列", type = EditType.TAB_TABLE_ADD)
    )
    private Set<BiColumn> biColumn;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "BI_ID")
    @EruptField(
            edit = @Edit(title = "图表", type = EditType.TAB_TABLE_ADD)
    )
    private Set<BiChart> biCharts;

}
