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
    Number("Statistic", "1-2 columns: value / [name]"),

    @ChartColumn({A.X})
    Alert("Alert", "1 column (multi-row auto-concatenated), highlights key information"),

    @ChartColumn({A.X, A.Y, A.Z})
    Line("Line", "2-3 columns: name / value / [category]"),
    @ChartColumn({A.X, A.Y, A.Z})
    StepLine("Step Line", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    Column("Bar", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    StackedColumn("Stacked Bar", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    Area("Area", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    PercentageArea("Percentage Area", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    Bar("Horizontal Bar", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    PercentStackedBar("Percentage Stacked Bar", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    Radar("Radar", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    Scatter("Scatter", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z, "zx"})
    Bubble("Bubble", "4 columns: x / y / series / size"),

    @ChartColumn({A.X, A.Y})
    Pie("Pie", "2 columns: name / value"),
    @ChartColumn({A.X, A.Y})
    Ring("Ring", Pie.desc),
    @ChartColumn({A.X, A.Y})
    Rose("Rose", Pie.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    RadialBar("Radial Bar", Line.desc),

    @ChartColumn({A.X, A.Y})
    Funnel("Funnel", Pie.desc),
    @ChartColumn({A.X, A.Y})
    Waterfall("Waterfall", "2 columns: name / incremental value"),
    @ChartColumn({A.X, A.Y, A.Z})
    WordCloud("Word Cloud", Line.desc),
    @ChartColumn({A.X, A.Y, A.Z})
    Sankey("Sankey", "3 columns: name / value / target name"),
    @ChartColumn({A.X, A.Y, A.Z})
    Chord("Chord", Sankey.desc),

    table("Table", "Any number of columns"),

    tpl("Component Template", null);

    private final String name;

    private final String desc;


}
