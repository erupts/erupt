package xyz.erupt.test.scope.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@EruptScope(ProjectScope.class)
@Erupt(name = "Scoped task", authVerify = false)
public class ScopedTask extends BaseModel {

    @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", notNull = true))
    private String name;

    @EruptField(views = @View(title = "Project"), edit = @Edit(title = "Project", notNull = true, search = @Search))
    private String project;

}
