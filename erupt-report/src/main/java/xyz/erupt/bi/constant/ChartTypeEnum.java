package xyz.erupt.bi.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YuePeng
 * date 2023/12/3 16:47
 */
@AllArgsConstructor
@Getter
public enum ChartTypeEnum {
    Number("数值统计", "需要2个数据列：名称，值"),
    Line("折线图", null),
    StepLine("阶梯折线图", null),
    Column("柱状图", null),
    StackedColumn("堆叠柱状图", null),
    Area("面积图", null),
    PercentageArea("百分比面积图", null),
    Bar("条形图", null),
    PercentStackedBar("百分比条形图", null),
    Scatter("散点图", null),
    Bubble("气泡图", "需要4个数据列：x / y / series / size"),

    Pie("饼图", null),
    Ring("环形图", null),
    Rose("玫瑰图", null),
    Radar("雷达图", null),
    RadialBar("玉珏图", null),

    Waterfall("瀑布图", "需要2个数据列：x:名称 y:增加或减少的值"),
    Funnel("漏斗图", null),

    WordCloud("词云", "x:名称, y:数值, [color:颜色]"),
    Sankey("桑基图", "需要3个数据列：source:名称, y:值,target:目标节点"),
    Chord("弦图", "需要3个数据列：source:名称, y:值,target:目标节点");

    private final String name;

    private final String desc;

}
