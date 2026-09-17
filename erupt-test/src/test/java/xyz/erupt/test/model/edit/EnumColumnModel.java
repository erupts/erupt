package xyz.erupt.test.model.edit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * The three ways an enum column can be declared, so that the ddl of each one can be asserted.
 */
@Getter
@Setter
@Entity
@Table(name = "e_test_enum_column")
@Erupt(name = "Enum Column")
public class EnumColumnModel extends BaseModel {

    @Enumerated(EnumType.STRING)
    @EruptField(views = @View(title = "Plain"), edit = @Edit(title = "Plain"))
    private Kind plain;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    @EruptField(views = @View(title = "Sized"), edit = @Edit(title = "Sized"))
    private Kind sized;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(8)")
    @EruptField(views = @View(title = "Declared"), edit = @Edit(title = "Declared"))
    private Kind declared;

    public enum Kind {
        A, B
    }

}
