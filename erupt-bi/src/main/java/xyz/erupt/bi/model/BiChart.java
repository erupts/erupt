package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.SliderType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2019-12-24.
 */
@Entity
@Table(name = "E_BI_CHART")
@Erupt(name = "图表配置")
@Getter
@Setter
public class BiChart extends BaseModel {

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

    @EruptField(
            views = @View(title = "栅格数"),
            edit = @Edit(title = "栅格数", type = EditType.SLIDER, desc = "单行可以显示的图表数量",
                    sliderType = @SliderType(max = 24, markPoints = {1, 2, 3, 4, 6, 8, 12}, dots = true))
    )
    private Integer grid = 1;

    @EruptField(
            views = @View(title = "联动查询"),
            edit = @Edit(title = "联动查询")
    )
    private Boolean linkage;

    @EruptField(
            views = @View(title = "显示顺序"),
            edit = @Edit(title = "显示顺序")
    )
    private Integer sort;

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
            edit = @Edit(title = "图表类型", notNull = true
                    , type = EditType.CHOICE, choiceType = @ChoiceType(vl = @VL(label = "饼形图", value = "pie")))
    )
    private String type;

    @Lob
    @EruptField(
            edit = @Edit(title = "图表sql", type = EditType.TEXTAREA)
    )
    private String sqlStatement;

    @Lob
    @EruptField(
            edit = @Edit(title = "自定义图表配置", desc = "JSON格式，参照echarts", type = EditType.TEXTAREA)
    )
    private String chartOption;

}
