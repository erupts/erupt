package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.bi.constant.ChartTypeEnum;
import xyz.erupt.bi.handler.ChartType;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import javax.persistence.*;

/**
 * @author YuePeng
 * date 2019-12-24.
 */
@Entity
@Table(name = "e_bi_chart", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "bi_id"}))
@Erupt(name = "图表配置", orderBy = "sort", dataProxy = BiChartDataProxy.class)
@Getter
@Setter
@EruptI18n
@Component
public class BiChart extends MetaModelUpdateVo {

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "编码", sortable = true, width = "100px")
    )
    private String code;

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "栅格数"),
            edit = @Edit(title = "栅格数", search = @Search(vague = true), type = EditType.SLIDER, desc = "图表占据的栅格数，24代表一行", notNull = true,
                    sliderType = @SliderType(max = 24, markPoints = {3, 4, 6, 8, 12, 16, 18, 20, 21, 24}, dots = true))
    )
    private Integer grid = 24;

    @EruptField(
            views = @View(title = "高度(px)"),
            edit = @Edit(title = "高度(px)", notNull = true)
    )
    private Integer height = 340;

    @EruptField(
            views = @View(title = "显示顺序", sortable = true),
            edit = @Edit(title = "显示顺序")
    )
    private Integer sort;

    @ManyToOne
    @EruptField(
            edit = @Edit(title = "处理类", type = EditType.REFERENCE_TABLE)
    )
    private BiClassHandler classHandler;

    @ManyToOne
    @EruptField(
            views = @View(title = "数据源", column = "name"),
            edit = @Edit(title = "数据源", type = EditType.REFERENCE_TREE, search = @Search)
    )
    private BiDataSource dataSource;

    @EruptField(
            views = @View(title = "描述"),
            edit = @Edit(title = "描述")
    )
    private String remark;

    @EruptField(
            views = @View(title = "缓存时间", sortable = true, template = "value&&value+'s'"),
            edit = @Edit(title = "缓存时间（秒）")
    )
    private Integer cacheTime = 1;


    @Enumerated(EnumType.STRING)
    @EruptField(
            views = @View(title = "图表类型"),
            edit = @Edit(title = "图表类型", notNull = true,
                    desc = "图表参考：G2Plot",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            type = ChoiceType.Type.RADIO,
                            fetchHandler = ChartType.class
                    ))
    )
    private ChartTypeEnum type;

    @ManyToOne
    @EruptField(
            edit = @Edit(title = "报表模板", type = EditType.REFERENCE_TABLE,
                    showBy = @ShowBy(dependField = "type", expr = "value=='tpl'"))
    )
    private BiTpl biTpl;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "图表SQL"),
            edit = @Edit(title = "图表SQL",
                    type = EditType.CODE_EDITOR, notNull = true, codeEditType = @CodeEditorType(language = "sql"))
    )
    private String sqlStatement;

    @Column(length = 4000)
    @EruptField(
            edit = @Edit(title = "自定义图表配置", desc = "JSON格式，参照G2Plot",
                    type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String chartOption;

    @ManyToOne
    private Bi bi;

}
