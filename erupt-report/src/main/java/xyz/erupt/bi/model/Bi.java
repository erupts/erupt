package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.*;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.bi.constant.BiConst;
import xyz.erupt.bi.handler.BiPublishMenu;
import xyz.erupt.bi.model.dataproxy.BiDataProxy;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import javax.persistence.*;
import java.util.Set;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(name = "报表配置",
        rowOperation = {
                @RowOperation(
                        title = "发布", mode = RowOperation.Mode.SINGLE, icon = "fa fa-send",
                        eruptClass = BiReleaseModal.class, operationHandler = BiPublishMenu.class
                )
        },
        orderBy = "createTime desc",
        linkTree = @LinkTree(field = "biGroup"),
        dataProxy = BiDataProxy.class,
        drills = {
                @Drill(title = "图表配置", icon = "fa fa-pie-chart"
                        , link = @Link(linkErupt = BiChart.class, joinColumn = "bi.id")),
                @Drill(title = "修改记录", icon = "fa fa-history text-green"
                        , link = @Link(linkErupt = BiHistory.class, joinColumn = "biId"))
        })
@Getter
@Setter
@Component
@EruptI18n
public class Bi extends MetaModelUpdateVo {

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "编码", width = "100px"),
            edit = @Edit(title = "编码", readonly = @Readonly(add = false), search = @Search, notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @ManyToOne
    @JoinColumn(name = "bi_group_id")
    @EruptField(
            views = @View(title = "组别", column = "name", sortable = true),
            edit = @Edit(title = "组别", notNull = true, type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parent.id"), search = @Search)
    )
    private BiGroup biGroup;

    @ManyToOne
    @JoinColumn(name = "datasource_id")
    @EruptField(
            views = @View(title = "数据源", column = "name"),
            edit = @Edit(title = "数据源", type = EditType.REFERENCE_TREE, search = @Search)
    )
    private BiDataSource dataSource;

    @ManyToOne
    @EruptField(
            edit = @Edit(title = "处理类", type = EditType.REFERENCE_TABLE)
    )
    private BiClassHandler classHandler;

    @EruptField(
            views = @View(title = "分页方式", sortable = true),
            edit = @Edit(title = "分页方式", notNull = true, type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
                    @VL(value = BiConst.PAGE_END, label = "后端分页"),
                    @VL(value = BiConst.PAGE_FRONT, label = "前端分页"),
                    @VL(value = BiConst.PAGE_NONE, label = "不分页"),
            }))
    )
    private String pageType = BiConst.PAGE_END;

    @EruptField(
            views = @View(title = "缓存时间", width = "100px", sortable = true, template = "value&&value+'s'"),
            edit = @Edit(title = "缓存时间（秒）")
    )
    private Integer cacheTime = 1;

    @EruptField(
            views = @View(title = "自刷周期", width = "100px", sortable = true, template = "value&&value+'s'"),
            edit = @Edit(title = "自动刷新周期（秒）",desc = "数据自动更新时间")
    )
    private Integer refreshTime;

    @EruptField(
            views = @View(title = "导出", sortable = true),
            edit = @Edit(title = "导出", notNull = true, boolType = @BoolType(trueText = "开启", falseText = "关闭"))
    )
    private Boolean export = true;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "取值SQL"),
            edit = @Edit(title = "取值SQL", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "sql"))
    )
    private String sqlStatement;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            edit = @Edit(title = "总条数SQL", desc = "计算数据总量，在取值SQL嵌套子查询count性能较低时使用",
                    showBy = @ShowBy(dependField = "pageType", expr = "value == '" + BiConst.PAGE_END + "'"),
                    type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "sql", height = 100))
    )
    private String countStatement;

    @Column(length = 2000)
    @EruptField(
            views = @View(title = "报表描述", type = ViewType.HTML),
            edit = @Edit(title = "报表描述", type = EditType.TEXTAREA)
    )
    private String remark;

    @Transient
    @EruptField(
            views = @View(title = "效果预览", type = ViewType.LINK_DIALOG, desc = "需提前配置菜单权限")
    )
    private String view;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bi_id")
    private Set<BiChart> biCharts;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "bi_id")
    @OrderBy("sort")
    @EruptField(
            edit = @Edit(title = "查询维度", type = EditType.TAB_TABLE_ADD)
    )
    private Set<BiDimension> biDimension;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "bi_id")
    @EruptField(
            edit = @Edit(title = "表格列配置", desc = "列会根据表达式动态渲染，不做列相关配置请忽略此项", type = EditType.TAB_TABLE_ADD)
    )
    private Set<BiColumn> biColumns;

}
