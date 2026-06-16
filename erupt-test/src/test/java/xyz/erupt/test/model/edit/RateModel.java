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

    // default 5 stars
    @EruptField(
            views = @View(title = "Rating"),
            edit = @Edit(title = "Rating", type = EditType.RATE)
    )
    private Integer rating;

    // custom count
    @EruptField(
            views = @View(title = "Score"),
            edit = @Edit(title = "Score", desc = "Out of 10",
                    type = EditType.RATE,
                    rateType = @RateType(count = 10))
    )
    private Integer score;

    // allow half-star
    @EruptField(
            views = @View(title = "Half Star"),
            edit = @Edit(title = "Half Star", type = EditType.RATE,
                    rateType = @RateType(allowHalf = true))
    )
    private Integer halfStar;

    // custom character (heart)
    @EruptField(
            views = @View(title = "Favorite"),
            edit = @Edit(title = "Favorite", type = EditType.RATE,
                    rateType = @RateType(character = "♥", count = 5))
    )
    private Integer favorite;

    // custom character + allow half
    @EruptField(
            views = @View(title = "Thumb"),
            edit = @Edit(title = "Thumb", type = EditType.RATE,
                    rateType = @RateType(character = "★", count = 6, allowHalf = true))
    )
    private Integer thumb;
}
