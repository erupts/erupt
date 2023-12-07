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
    Number("数值统计", "需要2个数据列：名称，值"),
    @ChartColumn({A.X, A.Y, A.Z})
    Line("折线图", null),
    @ChartColumn({A.X, A.Y, A.Z})
    StepLine("阶梯折线图", null),
    @ChartColumn({A.X, A.Y, A.Z})
    Column("柱状图", null),
    @ChartColumn({A.X, A.Y, A.Z})
    StackedColumn("堆叠柱状图", null),
    @ChartColumn({A.X, A.Y, A.Z})
    Area("面积图", null),
    @ChartColumn({A.X, A.Y, A.Z})
    PercentageArea("百分比面积图", null),
    @ChartColumn({A.X, A.Y, A.Z})
    Bar("条形图", null),
    @ChartColumn({A.X, A.Y, A.Z})
    PercentStackedBar("百分比条形图", null),
    @ChartColumn({A.X, A.Y, A.Z})
    Scatter("散点图", null),
    @ChartColumn({A.X, A.Y, A.Z, "zx"})
    Bubble("气泡图", "需要4个数据列：x / y / series / size"),

    @ChartColumn({A.X, A.Y})
    Pie("饼图", null),
    @ChartColumn({A.X, A.Y})
    Ring("环形图", null),
    @ChartColumn({A.X, A.Y})
    Rose("玫瑰图", null),
    @ChartColumn({A.X, A.Y, A.Z})
    Radar("雷达图", null),
    @ChartColumn({A.X, A.Y, A.Z})
    RadialBar("玉珏图", null),
    @ChartColumn({A.X, A.Y})
    Waterfall("瀑布图", "需要2个数据列：x:名称 y:增加或减少的值"),
    @ChartColumn({A.X, A.Y})
    Funnel("漏斗图", null),

    @ChartColumn({A.X, A.Y, A.Z})
    WordCloud("词云", "x:名称, y:数值, [color:颜色]"),
    @ChartColumn({A.X, A.Y, A.Z})
    Sankey("桑基图", "需要3个数据列：source:名称, y:值,target:目标节点"),
    @ChartColumn({A.X, A.Y, A.Z})
    Chord("弦图", "需要3个数据列：source:名称, y:值,target:目标节点"),

    table("数据表", "返回任意列数"),

    tpl("组件模板", null);

    private final String name;

    private final String desc;


}
