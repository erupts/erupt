package xyz.erupt.bi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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

import java.util.Set;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi", uniqueConstraints = @UniqueConstraint(name = "uk_bi_code", columnNames = "code"))
@Erupt(name = "Report Config",
        rowOperation = {
                @RowOperation(
                        title = "Add to Menu", mode = RowOperation.Mode.SINGLE, icon = "fa fa-send",
                        eruptClass = BiReleaseModal.class, operationHandler = BiPublishMenu.class
                )
        },
        orderBy = "createTime desc",
        linkTree = @LinkTree(field = "biGroup"),
        dataProxy = BiDataProxy.class,
        drills = {
                @Drill(title = "Chart Config", icon = "fa fa-pie-chart"
                        , link = @Link(linkErupt = BiChart.class, joinColumn = "bi.id")),
                @Drill(title = "Modification History", icon = "fa fa-history text-green"
                        , link = @Link(linkErupt = BiHistory.class, joinColumn = "biId"))
        })
@Getter
@Setter
@Component
@EruptI18n
public class Bi extends MetaModelUpdateVo {

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Code", width = "100px"),
            edit = @Edit(title = "Code", readonly = @Readonly(add = false), search = @Search, notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "Name", sortable = true),
            edit = @Edit(title = "Name", notNull = true, search = @Search)
    )
    private String name;

    @ManyToOne
    @JoinColumn(name = "bi_group_id")
    @EruptField(
            views = @View(title = "Group", column = "name", sortable = true),
            edit = @Edit(title = "Group", type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parent.id"), search = @Search)
    )
    private BiGroup biGroup;

    @ManyToOne
    @JoinColumn(name = "datasource_id")
    @EruptField(
            views = @View(title = "Data Source", column = "name"),
            edit = @Edit(title = "Data Source", type = EditType.REFERENCE_TREE, search = @Search)
    )
    private BiDataSource dataSource;

    @ManyToOne
    @EruptField(
            edit = @Edit(title = "Handler", type = EditType.REFERENCE_TABLE)
    )
    private BiClassHandler classHandler;

    @EruptField(
            views = @View(title = "Pagination", sortable = true),
            edit = @Edit(title = "Pagination", notNull = true, type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
                    @VL(value = BiConst.PAGE_END, label = "Backend Pagination"),
                    @VL(value = BiConst.PAGE_FRONT, label = "Frontend Pagination"),
                    @VL(value = BiConst.PAGE_NONE, label = "No Pagination"),
            }))
    )
    private String pageType = BiConst.PAGE_END;

    @EruptField(
            views = @View(title = "Cache Time", width = "100px", sortable = true, template = "value&&value+'s'"),
            edit = @Edit(title = "Cache Duration (s)")
    )
    private Integer cacheTime = 1;

    @EruptField(
            views = @View(title = "Refresh Interval", width = "100px", sortable = true, template = "value&&value+'s'"),
            edit = @Edit(title = "Auto Refresh (s)", desc = "Data auto-update interval")
    )
    private Integer refreshTime;

    @EruptField(
            views = @View(title = "Export", sortable = true),
            edit = @Edit(title = "Export", notNull = true, boolType = @BoolType(trueText = "On", falseText = "Off"))
    )
    private Boolean export = true;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Query SQL"),
            edit = @Edit(title = "Query SQL", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "sql"))
    )
    private String sqlStatement;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            edit = @Edit(title = "Count SQL", desc = "Compute total rows; use when nested count subquery is slow",
                    dynamic = @Dynamic(dependField = "pageType", condition = "value == '" + BiConst.PAGE_END + "'"),
                    type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "sql", height = 100))
    )
    private String countStatement;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Description", type = ViewType.HTML),
            edit = @Edit(title = "Description", type = EditType.TEXTAREA)
    )
    private String remark;

    @Transient
    @EruptField(
            views = @View(title = "Preview", type = ViewType.LINK_DIALOG, desc = "Requires menu permission configured in advance")
    )
    private String view;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "bi_id")
    @OrderBy("sort")
    @EruptField(
            edit = @Edit(title = "Query Dimensions", type = EditType.TAB_TABLE_ADD)
    )
    private Set<BiDimension> biDimension;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "bi_id")
    @EruptField(
            edit = @Edit(title = "Column Config", desc = "Columns render dynamically via expressions; skip if no config needed", type = EditType.TAB_TABLE_ADD)
    )
    private Set<BiColumn> biColumns;

}
