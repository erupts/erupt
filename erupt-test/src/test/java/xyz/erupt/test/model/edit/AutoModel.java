package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

import java.util.Date;

@Getter
@Setter
@Entity
@Erupt(name = "AutoEdit")
public class AutoModel extends BaseModel {

    @EruptField(
            views = @View(title = "String"),
            edit = @Edit(title = "String")
    )
    private String value;

    @EruptField(
            views = @View(title = "Date"),
            edit = @Edit(title = "Date")
    )
    private Date date;

    @EruptField(
            views = @View(title = "Number"),
            edit = @Edit(title = "Number")
    )
    private Integer number;
}
