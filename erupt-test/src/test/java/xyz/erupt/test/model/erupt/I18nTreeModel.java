package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * Tree entity annotated with @EruptI18n: its tree labels are translated through the i18n CSV
 * according to the request "Lang" header. Compare with {@link TreeModel}, which carries no
 * annotation and therefore returns raw labels.
 */
@Getter
@Setter
@Entity
@Erupt(name = "I18n Tree Demo",
        tree = @Tree(label = "name", pid = "parentId")
)
@EruptI18n
public class I18nTreeModel extends BaseModel {

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
