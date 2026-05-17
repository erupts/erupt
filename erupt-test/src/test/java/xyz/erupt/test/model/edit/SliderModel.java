package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.SliderType;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "SliderEdit")
public class SliderModel extends BaseModel {

    // 基础 min/max
    @EruptField(
            views = @View(title = "Percentage"),
            edit = @Edit(title = "Percentage", type = EditType.SLIDER,
                    sliderType = @SliderType(min = 0, max = 100))
    )
    private Integer percentage;

    // 指定步长
    @EruptField(
            views = @View(title = "Step Size"),
            edit = @Edit(title = "Step Size", desc = "Step: 5",
                    type = EditType.SLIDER,
                    sliderType = @SliderType(min = 0, max = 100, step = 5))
    )
    private Integer stepValue;

    // 刻度标记点
    @EruptField(
            views = @View(title = "Mark Points"),
            edit = @Edit(title = "Mark Points", type = EditType.SLIDER,
                    sliderType = @SliderType(min = 0, max = 100,
                            markPoints = {0, 25, 50, 75, 100}))
    )
    private Integer markedValue;

    // 只能落在刻度点上
    @EruptField(
            views = @View(title = "Dots Only"),
            edit = @Edit(title = "Dots Only", desc = "Slider snaps to mark points",
                    type = EditType.SLIDER,
                    sliderType = @SliderType(min = 0, max = 100,
                            markPoints = {0, 25, 50, 75, 100}, dots = true))
    )
    private Integer dotsValue;

    // 负数范围
    @EruptField(
            views = @View(title = "Temperature"),
            edit = @Edit(title = "Temperature", desc = "-50 ~ 50 °C",
                    type = EditType.SLIDER,
                    sliderType = @SliderType(min = -50, max = 50))
    )
    private Integer temperature;
}
