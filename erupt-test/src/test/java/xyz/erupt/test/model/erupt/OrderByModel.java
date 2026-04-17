package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * 演示 orderBy：列表默认按 sort 升序、name 降序排列
 */
@Getter
@Setter
@Entity
@Erupt(name = "OrderBy Demo",
        orderBy = "sort asc"
)
public class OrderByModel extends BaseModel {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "Sort"),
            edit = @Edit(title = "Sort")
    )
    private Integer sort;

    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark")
    )
    private String remark;
}
