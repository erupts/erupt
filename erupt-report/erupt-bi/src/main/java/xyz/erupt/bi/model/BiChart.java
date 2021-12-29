package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.annotations.Type;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import javax.annotation.Resource;
import javax.persistence.*;
import java.util.Date;

/**
 * @author YuePeng
 * date 2019-12-24.
 */
@Entity
@Table(name = "e_bi_chart", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "bi_id"}))
@Erupt(name = "图表配置", orderBy = "sort", dataProxy = BiChart.class)
@Getter
@Setter
@EruptI18n
@Component
public class BiChart extends MetaModelUpdateVo implements DataProxy<BiChart> {

    @EruptField(
            views = @View(title = "编码", sortable = true, width = "90px")
    )
    private String code;

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "栅格数"),
            edit = @Edit(title = "栅格数", search = @Search(vague = true), type = EditType.SLIDER, desc = "单行可以显示的图表数量", notNull = true,
                    sliderType = @SliderType(max = 12, markPoints = {1, 2, 3, 4, 6, 8, 12}, dots = true))
    )
    private Integer grid = 1;

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

    /**
     * 单数值
     * 饼图
     * 柱状图
     * 堆积图
     * 线形图
     * 蜘蛛图
     */
    @EruptField(
            views = @View(title = "图表类型"),
            edit = @Edit(title = "图表类型", notNull = true,
                    desc = "图表参考：G2Plot",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            type = ChoiceType.Type.RADIO,
                            vl = {
                                    //------------
                                    @VL(label = "折线图", value = "Line"),
                                    @VL(label = "阶梯折线图", value = "StepLine"),
                                    @VL(label = "柱状图", value = "Column"),
                                    @VL(label = "堆叠柱状图", value = "StackedColumn"),
                                    @VL(label = "面积图", value = "Area"),
                                    @VL(label = "百分比面积图", value = "PercentageArea"),
                                    @VL(label = "条形图", value = "Bar"),
                                    @VL(label = "百分比条形图", value = "PercentStackedBar"),
                                    @VL(label = "散点图", value = "Scatter"),
                                    //------------
                                    @VL(label = "饼图", value = "Pie"),
                                    @VL(label = "环形图", value = "Ring"),
                                    @VL(label = "玫瑰图", value = "Rose"),
                                    @VL(label = "雷达图", value = "Radar"),
                                    //------------
                                    @VL(label = "气泡图", value = "Bubble", desc = "需要4个数据列：x / y / series / size"),
                                    @VL(label = "瀑布图", value = "Waterfall", desc = "需要2个数据列：x:名称 y:增加或减少的值"),
                                    @VL(label = "漏斗图", value = "Funnel"),
                                    //@VL(label = "词云", value = "WordCloud", desc = "需要2个数据列：x:名称 y:数值"),
                                    //@VL(label = "热力图", value = "Heatmap", desc = "最少需要3个数据列，size可选：x / y / value / [size]"),
                                    @VL(label = "自定义模板", value = "tpl", desc = "使用前请确认是否导入erupt-tpl模块"),
                            }
                    ))
    )
    private String type;

    @EruptField(
            edit = @Edit(title = "模板地址", desc = "项目内路径，解析方式为freemarker，可通过data获取图表数据，option获取自定义图表参数",
                    showBy = @ShowBy(dependField = "type", expr = "value=='tpl'"))
    )
    private String path;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "图表SQL"),
            edit = @Edit(title = "图表SQL", desc = "规则：二维切片，三维切块，维度顺序：X -> Y -> Series",
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

    @Resource
    @Transient
    private EruptDao eruptDao;

    @Override
    public void beforeAdd(BiChart biChart) {
        if (null == biChart.getSort()) {
            Integer obj = (Integer) eruptDao.getEntityManager().createQuery(
                    "select max(sort) from " + BiChart.class.getSimpleName() + " where bi.id = " + biChart.getBi().getId()).getSingleResult();
            biChart.setSort((obj == null) ? 10 : obj + 10);
        }
        biChart.setCode(RandomStringUtils.randomAlphabetic(6));
    }

    @Override
    public void beforeUpdate(BiChart biChart) {
        eruptDao.getEntityManager().clear();
        BiChart hbc = eruptDao.getEntityManager().find(BiChart.class, biChart.getId());
        if (!biChart.getSqlStatement().equals(hbc.getSqlStatement())) {
            BiHistory history = new BiHistory();
            history.setBi(biChart.getBi());
            history.setSqlStatement(hbc.getSqlStatement());
            history.setOperateTime(new Date());
            history.setMark(biChart.getName());
            history.setOperateBy("adf");
            eruptDao.persistAndFlush(history);
        }
    }

}
