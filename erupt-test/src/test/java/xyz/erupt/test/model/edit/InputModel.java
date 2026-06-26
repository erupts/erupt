package xyz.erupt.test.model.edit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "InputEdit")
public class InputModel extends BaseModel {

    // required + placeholder
    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, placeHolder = "Enter name",
                    type = EditType.INPUT)
    )
    private String name;

    // length limit + full span
    @Column(length = 500)
    @EruptField(
            views = @View(title = "Description"),
            edit = @Edit(title = "Description", desc = "Max 500 chars",
                    type = EditType.INPUT,
                    inputType = @InputType(length = 500, fullSpan = true))
    )
    private String description;

    // email type
    @EruptField(
            views = @View(title = "Email"),
            edit = @Edit(title = "Email", type = EditType.INPUT,
                    inputType = @InputType(type = "email"))
    )
    private String email;

    // regex validation (phone number)
    @EruptField(
            views = @View(title = "Phone"),
            edit = @Edit(title = "Phone", type = EditType.INPUT,
                    inputType = @InputType(regex = "^1[3-9]\\d{9}$"))
    )
    private String phone;

    // disable auto-trim
    @EruptField(
            views = @View(title = "Code"),
            edit = @Edit(title = "Code", desc = "Whitespace is preserved",
                    type = EditType.INPUT,
                    inputType = @InputType(autoTrim = false))
    )
    private String code;

    // prefix + suffix
    @EruptField(
            views = @View(title = "Price"),
            edit = @Edit(title = "Price", type = EditType.INPUT,
                    inputType = @InputType(
                            prefix = {@VL(value = "¥", label = "CNY")},
                            suffix = {@VL(value = "Yuan", label = "Yuan")}))
    )
    private String price;

    // enable fuzzy search
    @EruptField(
            views = @View(title = "Keyword"),
            edit = @Edit(title = "Keyword", type = EditType.INPUT,
                    search = @Search)
    )
    private String keyword;

    // read-only in edit mode
    @EruptField(
            views = @View(title = "Serial No"),
            edit = @Edit(title = "Serial No",
                    readonly = @Readonly(add = false, edit = true),
                    type = EditType.INPUT)
    )
    private String serialNo;

    // hidden in form (list view only)
    @EruptField(
            views = @View(title = "Internal Code"),
            edit = @Edit(title = "Internal Code", show = false, type = EditType.INPUT)
    )
    private String internalCode;
}
