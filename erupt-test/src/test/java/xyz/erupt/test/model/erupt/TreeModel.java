package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * 演示 @Tree：自引用树形结构，通过 pid 字段标识父节点，默认展开 2 级
 */
@Getter
@Setter
@Entity
@Erupt(name = "Tree Demo",
        tree = @Tree(
                id = "id",
                label = "name",
                pid = "parentId",
                expandLevel = 2
        )
)
public class TreeModel extends BaseModel {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "Parent ID"),
            edit = @Edit(title = "Parent ID")
    )
    private Long parentId;

    @EruptField(
            views = @View(title = "Sort"),
            edit = @Edit(title = "Sort")
    )
    private Integer sort;

    @EruptField(
            views = @View(title = "Icon"),
            edit = @Edit(title = "Icon")
    )
    private String icon;
}
