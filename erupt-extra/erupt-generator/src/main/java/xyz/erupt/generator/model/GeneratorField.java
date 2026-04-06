package xyz.erupt.generator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Dynamic;
import xyz.erupt.generator.base.GeneratorType;
import xyz.erupt.jpa.model.BaseModel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@EruptI18n
@Erupt(name = "Erupt Field Info")
@Table(name = "e_generator_field")
@Entity
@Getter
@Setter
public class GeneratorField extends BaseModel implements ChoiceFetchHandler {

    @EruptField(
            views = @View(title = "Field Name"),
            edit = @Edit(title = "Field Name", notNull = true,
                    desc = "Camel case naming, start with lowercase")
    )
    private String fieldName;

    @EruptField(
            views = @View(title = "Display Name"),
            edit = @Edit(title = "Display Name", notNull = true)
    )
    private String showName;

    @EruptField(
            views = @View(title = "Display Order", sortable = true),
            edit = @Edit(title = "Display Order", notNull = true)
    )
    private Integer sort;

    @Enumerated(EnumType.STRING)
    @EruptField(
            views = @View(title = "Edit Type"),
            edit = @Edit(title = "Edit Type",
                    notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(type = ChoiceType.Type.RADIO, fetchHandler = GeneratorField.class))
    )
    private GeneratorType type = GeneratorType.INPUT;

    @EruptField(
            views = @View(title = "Related Entity"),
            edit = @Edit(title = "Related Entity", dynamic = @Dynamic(dependField = "type",
                    match = Dynamic.Ctrl.NOTNULL,
                    condition = "value.indexOf('REFERENCE') !== -1 || value.indexOf('TAB') !== -1 || value === 'CHECKBOX' || value === 'COMBINE'"))
    )
    private String linkClass;

    @EruptField(
            views = @View(title = "Query Item"),
            edit = @Edit(title = "Query Item", notNull = true)
    )
    private Boolean query = true;

    @EruptField(
            views = @View(title = "Field Sort"),
            edit = @Edit(title = "Field Sort", notNull = true)
    )
    private Boolean sortable = false;

    @EruptField(
            views = @View(title = "Required"),
            edit = @Edit(title = "Required", notNull = true)
    )
    private Boolean notNull = true;

    @EruptField(
            views = @View(title = "Visible"),
            edit = @Edit(title = "Visible", notNull = true)
    )
    private Boolean isShow = true;


    @Override
    public List<VLModel> fetch(String[] params) {
        return Arrays.stream(GeneratorType.values()).map(it -> new VLModel(it.name(), it.getName(), it.name())).collect(Collectors.toList());
    }

}
