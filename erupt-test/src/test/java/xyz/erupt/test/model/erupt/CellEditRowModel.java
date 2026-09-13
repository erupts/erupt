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
 * Proves that a single-cell update is validated as a whole row: its DataProxy carries a
 * cross-field rule that no individual field violates on its own.
 */
@Getter
@Setter
@Entity
@Erupt(name = "CellEdit Whole Row",
        authVerify = false,
        dataProxy = CellEditRowDataProxy.class
)
public class CellEditRowModel extends BaseModel {

    @EruptField(
            views = @View(title = "Title"),
            edit = @Edit(title = "Title", notNull = true)
    )
    private String title;

    @EruptField(
            views = @View(title = "Content"),
            edit = @Edit(title = "Content")
    )
    private String content;

    @EruptField(
            views = @View(title = "Status"),
            edit = @Edit(title = "Status")
    )
    private String status;
}
