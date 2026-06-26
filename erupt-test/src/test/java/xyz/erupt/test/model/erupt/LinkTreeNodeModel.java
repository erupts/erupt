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
 * Demonstrates left-tree-right-table: the tree node side (category tree).
 * Used together with LinkTreeDetailModel.
 */
@Getter
@Setter
@Entity
@Erupt(name = "LinkTree - Category Node",
        tree = @Tree(
                id = "id",
                label = "name",
                pid = "parentId",
                expandLevel = 999
        )
)
public class LinkTreeNodeModel extends BaseModel {

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
}
