package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "BooleanEdit")
public class BooleanModel extends BaseModel {

    // 默认 Y/N 文本
    @EruptField(
            views = @View(title = "Active"),
            edit = @Edit(title = "Active", type = EditType.BOOLEAN,
                    search = @Search)
    )
    private Boolean active;

    // 自定义 trueText / falseText
    @EruptField(
            views = @View(title = "Enabled"),
            edit = @Edit(title = "Enabled", type = EditType.BOOLEAN,
                    boolType = @BoolType(trueText = "Enabled", falseText = "Disabled"))
    )
    private Boolean enabled;

    // 中文文本
    @EruptField(
            views = @View(title = "Published"),
            edit = @Edit(title = "Published", type = EditType.BOOLEAN,
                    boolType = @BoolType(trueText = "已发布", falseText = "未发布"))
    )
    private Boolean published;

    // 必填
    @EruptField(
            views = @View(title = "Agreed"),
            edit = @Edit(title = "Agreed", notNull = true, type = EditType.BOOLEAN,
                    boolType = @BoolType(trueText = "Yes", falseText = "No"))
    )
    private Boolean agreed;
}
