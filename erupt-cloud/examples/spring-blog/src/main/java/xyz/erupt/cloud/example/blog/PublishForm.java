package xyz.erupt.cloud.example.blog;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.jpa.model.BaseModel;

import java.time.LocalDate;

/**
 * Form of the Publish row operation; not an entity, never stored.
 */
@Erupt(name = "Publish Options")
public class PublishForm extends BaseModel {

    @EruptField(edit = @Edit(title = "Publish Date", notNull = true))
    private LocalDate publishDate;

    public LocalDate getPublishDate() {
        return publishDate;
    }

}
