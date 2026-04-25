package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "NumberEdit")
public class NumberModel extends BaseModel {

    // min + max 双限制
    @EruptField(
            views = @View(title = "Age"),
            edit = @Edit(title = "Age", type = EditType.NUMBER,
                    numberType = @NumberType(min = 0, max = 150))
    )
    private Integer age;

    // 仅限非负数
    @EruptField(
            views = @View(title = "Quantity"),
            edit = @Edit(title = "Quantity", notNull = true, type = EditType.NUMBER,
                    numberType = @NumberType(min = 0))
    )
    private Integer quantity;

    // 允许负数
    @EruptField(
            views = @View(title = "Balance"),
            edit = @Edit(title = "Balance", type = EditType.NUMBER,
                    numberType = @NumberType(min = -999999, max = 999999))
    )
    private Long balance;

    // 百分比（0-100）+ 搜索
    @EruptField(
            views = @View(title = "Percent"),
            edit = @Edit(title = "Percent", desc = "0 ~ 100",
                    type = EditType.NUMBER,
                    numberType = @NumberType(min = 0, max = 100),
                    search = @Search)
    )
    private Integer percent;
}
