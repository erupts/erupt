package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Layout;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * Demonstrates @Layout: frontend paging, full-line form, auto-refresh every 5 seconds.
 */
@Getter
@Setter
@Entity
@Erupt(name = "Layout - Front Paging & Auto Refresh",
        layout = @Layout(
                pagingType = Layout.PagingType.FRONT,
                pageSize = 10,
                formSize = Layout.FormSize.FULL_LINE,
                refreshTime = 5000
        )
)
public class LayoutFrontPageModel extends BaseModel {

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
            views = @View(title = "Score"),
            edit = @Edit(title = "Score")
    )
    private Integer score;
}
