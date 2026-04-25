package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.AutoCompleteType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "AutoCompleteEdit")
public class AutoCompleteModel extends BaseModel {

    // 默认触发长度（1 个字符即触发）
    @EruptField(
            views = @View(title = "City"),
            edit = @Edit(title = "City", type = EditType.AUTO_COMPLETE,
                    autoCompleteType = @AutoCompleteType(handler = TestAutoCompleteHandler.class),
                    search = @Search(value = true, vague = true))
    )
    private String city;

    // 至少输入 2 个字符才触发
    @EruptField(
            views = @View(title = "Company"),
            edit = @Edit(title = "Company", type = EditType.AUTO_COMPLETE,
                    autoCompleteType = @AutoCompleteType(
                            handler = TestAutoCompleteHandler.class,
                            triggerLength = 2))
    )
    private String company;

    // 携带额外参数给 handler
    @EruptField(
            views = @View(title = "Product"),
            edit = @Edit(title = "Product", type = EditType.AUTO_COMPLETE,
                    autoCompleteType = @AutoCompleteType(
                            handler = TestAutoCompleteHandler.class,
                            param = {"category:electronics"},
                            triggerLength = 1))
    )
    private String product;
}
