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
 * A tree capped at two levels: roots and their children, nothing deeper.
 */
@Getter
@Setter
@Entity
@Erupt(name = "Max Level Tree", authVerify = false,
        tree = @Tree(id = "id", label = "name", pid = "parentId", maxLevel = 2))
public class MaxLevelTreeModel extends BaseModel {

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
