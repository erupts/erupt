package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.RateType;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "RateEdit")
public class RateModel extends BaseModel {

    // 默认 5 星
    @EruptField(
            views = @View(title = "Rating"),
            edit = @Edit(title = "Rating", type = EditType.RATE)
    )
    private Integer rating;

    // 自定义总数
    @EruptField(
            views = @View(title = "Score"),
            edit = @Edit(title = "Score", desc = "Out of 10",
                    type = EditType.RATE,
                    rateType = @RateType(count = 10))
    )
    private Integer score;

    // 允许半星
    @EruptField(
            views = @View(title = "Half Star"),
            edit = @Edit(title = "Half Star", type = EditType.RATE,
                    rateType = @RateType(allowHalf = true))
    )
    private Integer halfStar;

    // 自定义字符（爱心）
    @EruptField(
            views = @View(title = "Favorite"),
            edit = @Edit(title = "Favorite", type = EditType.RATE,
                    rateType = @RateType(character = "♥", count = 5))
    )
    private Integer favorite;

    // 自定义字符 + 允许半选
    @EruptField(
            views = @View(title = "Thumb"),
            edit = @Edit(title = "Thumb", type = EditType.RATE,
                    rateType = @RateType(character = "★", count = 6, allowHalf = true))
    )
    private Integer thumb;
}
