package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.*;
import java.util.Set;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
@Entity
@Table(name = "E_BI", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(name = "报表配置", drills = @Drill(code = "chart", title = "图表配置", icon = "fa fa-pie-chart"
        , link = @Link(eruptClass = BiChart.class, joinColumn = "bi.id")))
@Getter
@Setter
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
            edit = @Edit(title = "数据源", type = EditType.REFERENCE_TREE, search = @Search(value = true))
    )
    private BiDataSource dataSource;

    @EruptField(
            edit = @Edit(title = "handler", placeHolder = "处理类", desc = "需实现xyz.erupt.bi.fun.BiHandler接口")
    )
    private String handler;

    @EruptField(
            views = @View(title = "是否可导出"),
            edit = @Edit(title = "是否可导出", search = @Search(true))
    )
    private Boolean export;

    @Lob
    @EruptField(
            views = @View(title = "SQL语句"),
            edit = @Edit(title = "SQL语句", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "sql"))
    )
    private String sqlStatement;

//    @Lob
//    @EruptField(
//            edit = @Edit(title = "总条数sql", type = EditType.TEXTAREA)
//    )
//    private String countSql;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "BI_ID")
//    @EruptField(
//            edit = @Edit(title = "图表", type = EditType.TAB_TABLE_ADD, orderBy = "sort")
//    )
    private Set<BiChart> biCharts;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "BI_ID")
    @EruptField(
            edit = @Edit(title = "查询维度", type = EditType.TAB_TABLE_ADD)
    )
    private Set<BiDimension> biDimension;

}
