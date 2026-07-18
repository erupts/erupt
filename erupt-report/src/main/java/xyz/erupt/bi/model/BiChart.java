package xyz.erupt.bi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.*;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.bi.constant.ChartTypeEnum;
import xyz.erupt.bi.handler.ChartType;
import xyz.erupt.bi.model.dataproxy.BiChartDataProxy;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * @author YuePeng
 * date 2019-12-24.
 */
@Entity
@Table(name = "e_bi_chart", uniqueConstraints = @UniqueConstraint(name = "uk_bi_chart_code_bi", columnNames = {"code", "bi_id"}))
@Erupt(name = "Chart Config", orderBy = "sort", dataProxy = BiChartDataProxy.class)
@Getter
@Setter
@EruptI18n
@Component
public class BiChart extends MetaModelUpdateVo {

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Code", sortable = true, width = "100px"),
            edit = @Edit(title = "Code", readonly = @Readonly(add = false), notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "Name", sortable = true),
            edit = @Edit(title = "Name", notNull = true, search = @Search)
    )
    private String name;

    @EruptField(
            views = @View(title = "Grid", type = ViewType.PROGRESS),
            edit = @Edit(title = "Grid", search = @Search, type = EditType.SLIDER, desc = "Grid units the chart occupies; 24 = full row", notNull = true,
                    sliderType = @SliderType(max = 24, markPoints = {3, 4, 6, 8, 12, 16, 18, 20, 21, 24}, dots = true))
    )
    private Integer grid = 24;

    @EruptField(
            views = @View(title = "Height (px)"),
            edit = @Edit(title = "Height (px)", notNull = true)
    )
    private Integer height = 340;

    @ManyToOne
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
            views = @View(title = "Sort", sortable = true),
            edit = @Edit(title = "Sort")
    )
    private Integer sort;

    @EruptField(
            views = @View(title = "Cache Time", sortable = true, template = "value&&value+'s'"),
            edit = @Edit(title = "Cache Duration (s)")
    )
    private Integer cacheTime = 1;

    @EruptField(
            views = @View(title = "Description"),
            edit = @Edit(title = "Description", inputType = @InputType(fullSpan = true))
    )
    private String remark;

    @Enumerated(EnumType.STRING)
    @EruptField(
            views = @View(title = "Chart Type"),
            edit = @Edit(title = "Chart Type", notNull = true,
                    desc = "Reference: G2Plot",
                    type = EditType.CHOICE,
                    search = @Search,
                    choiceType = @ChoiceType(
                            type = ChoiceType.Type.RADIO,
                            fetchHandler = ChartType.class
                    ))
    )
    private ChartTypeEnum type;

    @ManyToOne
    @EruptField(
            edit = @Edit(title = "Report Template", type = EditType.REFERENCE_TABLE,
                    dynamic = @Dynamic(dependField = "type", condition = "value=='tpl'", match = Dynamic.Ctrl.NOTNULL))
    )
    private BiTpl biTpl;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Chart SQL"),
            edit = @Edit(title = "Chart SQL", search = @Search,
                    type = EditType.CODE_EDITOR, notNull = true, codeEditType = @CodeEditorType(language = "sql"))
    )
    private String sqlStatement;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(title = "Custom Chart Config", desc = "JSON format, reference G2Plot",
                    type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String chartOption;

    @ManyToOne
    private Bi bi;

}
