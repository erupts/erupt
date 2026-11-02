package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

import java.util.Set;

// a tab referring to its own model, like the predecessors of a gantt task: building it must not recurse forever
@Getter
@Setter
@Entity
@Erupt(name = "SelfReferEdit")
public class SelfReferModel extends BaseModel {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name")
    )
    private String name;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "self_refer_id"),
            inverseJoinColumns = @JoinColumn(name = "predecessor_id"))
    @EruptField(
            edit = @Edit(title = "Predecessors", type = EditType.TAB_TABLE_REFER)
    )
    private Set<SelfReferModel> predecessors;
}
