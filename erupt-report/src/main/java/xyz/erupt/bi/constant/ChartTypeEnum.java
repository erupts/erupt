package xyz.erupt.bi.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.erupt.bi.annotation.ChartColumn;

/**
 * @author YuePeng
 * date 2023/12/3 16:47
 */
@AllArgsConstructor
@Getter
public enum ChartTypeEnum {

    @ChartColumn({A.X, A.Y})
    Number("数值统计", "1 ~ 2 个数据列：值 / [名称]"),

    @ChartColumn({A.X})
    Alert("文本提示", "1 个数据列, 展现需要关注的信息"),

    @ChartColumn({A.X, A.Y, A.Z})
    Line("折线图", "2 ~ 3 个数据列：名称 / 值 / [分类]"),
    @ChartColumn({A.X, A.Y, A.Z})
    StepLine("阶梯折线图", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    Column("柱状图", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    StackedColumn("堆叠柱状图", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    Area("面积图", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    PercentageArea("百分比面积图", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    Bar("条形图", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    PercentStackedBar("百分比条形图", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    Radar("雷达图", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    Scatter("散点图", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z, "zx"})
    Bubble("气泡图", "4个数据列：x / y / series / size"),

    @ChartColumn({A.X, A.Y})
    Pie("饼图", "2 个数据列：名称 / 值"),
    @ChartColumn({A.X, A.Y})
    Ring("环形图", Pie.desc),
    @ChartColumn({A.X, A.Y})
    Rose("玫瑰图", Pie.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    RadialBar("玉珏图", Line.desc),

    @ChartColumn({A.X, A.Y})
    Funnel("漏斗图", Pie.desc),
    @ChartColumn({A.X, A.Y})
    Waterfall("瀑布图", "2 个数据列：名称 / 增加或减少的值"),
    @ChartColumn({A.X, A.Y, A.Z})
    WordCloud("词云", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    Sankey("桑基图", "3 个数据列：名称 / 值 / 目标名"),
    @ChartColumn({A.X, A.Y, A.Z})
    Chord("弦图", Sankey.desc),

    table("数据表", "任意列数"),

    tpl("组件模板", null);

    private final String name;

    private final String desc;


}
