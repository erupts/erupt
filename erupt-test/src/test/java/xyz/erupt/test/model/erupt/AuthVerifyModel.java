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
 * Demonstrates authVerify = false: skips permission check, accessible to everyone.
 * Suitable for public data endpoints such as dictionaries or announcements.
 */
@Getter
@Setter
@Entity
@Erupt(name = "AuthVerify - Public Access",
        authVerify = false
)
public class AuthVerifyModel extends BaseModel {

    @EruptField(
            views = @View(title = "Key"),
            edit = @Edit(title = "Key", notNull = true)
    )
    private String key;

    @EruptField(
            views = @View(title = "Value"),
            edit = @Edit(title = "Value", notNull = true)
    )
    private String value;

    @EruptField(
            views = @View(title = "Description"),
            edit = @Edit(title = "Description")
    )
    private String description;
}
