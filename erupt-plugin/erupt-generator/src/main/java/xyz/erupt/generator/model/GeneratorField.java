package xyz.erupt.generator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
import xyz.erupt.core.i18n.I18nTranslate;
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
public class GeneratorField extends BaseModel implements ChoiceFetchHandler<Void> {

    @EruptField(
            views = @View(title = "Field Name"),
            edit = @Edit(title = "Field Name", notNull = true,
                    desc = "Camel case naming, start with lowercase")
    )
    private String fieldName;

    @EruptField(
            views = @View(title = "Column Name", show = false),
            edit = @Edit(title = "Column Name", desc = "Leave empty when it matches the field name")
    )
    private String columnName;

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
            views = @View(title = "Java Type", show = false),
            edit = @Edit(title = "Java Type", desc = "Overrides the type inferred from the edit type, e.g. Long / BigDecimal")
    )
    private String javaType;

    @EruptField(
            views = @View(title = "Length", show = false),
            edit = @Edit(title = "Length", desc = "Column length, only rendered for text columns")
    )
    private Integer length;

    @EruptField(
            views = @View(title = "Component Config", show = false),
            edit = @Edit(title = "Component Config", type = EditType.TEXTAREA,
                    desc = "Replaces the component configuration the edit type would generate, e.g. choiceType = @ChoiceType(...)")
    )
    private String typeCode;

    @EruptField(
            views = @View(title = "Related Entity"),
            edit = @Edit(title = "Related Entity", dynamic = @Dynamic(dependField = "type",
                    match = Dynamic.Ctrl.NOTNULL,
                    condition = "value.indexOf('REFERENCE') !== -1 || value.indexOf('TAB') !== -1 || value === 'CHECKBOX' || value === 'COMBINE'"))
    )
    private String linkClass;

    @EruptField(
            views = @View(title = "Primary Key", show = false),
            edit = @Edit(title = "Primary Key", notNull = true, desc = "Only used when the entity has no parent class")
    )
    private Boolean primaryKey = false;

    @EruptField(
            views = @View(title = "Auto Increment", show = false),
            edit = @Edit(title = "Auto Increment", notNull = true)
    )
    private Boolean autoIncrement = false;

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
        return Arrays.stream(GeneratorType.values()).map(it -> new VLModel(it.name(), I18nTranslate.$translate(it.getName()), it.name())).collect(Collectors.toList());
    }

}
