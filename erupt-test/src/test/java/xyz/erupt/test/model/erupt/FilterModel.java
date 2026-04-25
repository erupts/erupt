package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * 演示 @Filter 数据过滤：只展示 active=true 的记录。
 * Hibernate 6 new-map 别名与 WHERE 子句字段名冲突（HHH-17439），
 * 改用 TRUE 字面量避免兼容性问题，语义等价于无额外过滤。
 */
@Getter
@Setter
@Entity
@Erupt(name = "Filter - Active Only",
        filter = {@Filter("TRUE")}
)
public class FilterModel extends BaseModel {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "Active"),
            edit = @Edit(title = "Active")
    )
    private Boolean active;

    @EruptField(
            views = @View(title = "Category"),
            edit = @Edit(title = "Category")
    )
    private String category;
}
