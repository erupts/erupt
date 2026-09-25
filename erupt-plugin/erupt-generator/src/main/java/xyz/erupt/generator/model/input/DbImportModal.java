package xyz.erupt.generator.model.input;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.generator.base.SuperModel;
import xyz.erupt.generator.handler.DbChoiceFetch;
import xyz.erupt.generator.handler.SuperModelChoice;
import xyz.erupt.generator.handler.DbColumnTags;
import xyz.erupt.jpa.model.BaseModel;

import java.util.Set;

/**
 * Form of the "Import from Database" button, the datasource, the database and the
 * table list are chained, each one reloads the next.
 *
 * @author YuePeng
 * date 2026-09-17
 */
@Erupt(name = "Import from Database")
@EruptI18n
@Getter
@Setter
public class DbImportModal extends BaseModel {

    @EruptField(
            edit = @Edit(title = "Datasource", notNull = true, type = EditType.CHOICE,
                    desc = "Every datasource registered in the application",
                    choiceType = @ChoiceType(fetchHandler = DbChoiceFetch.class, fetchHandlerParams = DbChoiceFetch.DATA_SOURCE))
    )
    private String dataSource;

    @EruptField(
            edit = @Edit(title = "Database", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = DbChoiceFetch.class,
                            fetchHandlerParams = DbChoiceFetch.NAMESPACE, dependField = "dataSource"))
    )
    private String namespace;

    @EruptField(
            edit = @Edit(title = "Tables", notNull = true, type = EditType.MULTI_CHOICE,
                    multiChoiceType = @MultiChoiceType(type = MultiChoiceType.Type.TRANSFER,
                            fetchHandler = DbChoiceFetch.class,
                            fetchHandlerParams = DbChoiceFetch.TABLE, dependField = "namespace"))
    )
    private Set<String> tables;

    @EruptField(
            edit = @Edit(title = "Ignore Columns", type = EditType.TAGS,
                    desc = "Columns left out of the entity, picked from the selected tables or typed as a pattern like tenant_*",
                    tagsType = @TagsType(joinSeparator = ",", fetchHandler = DbColumnTags.class))
    )
    private String ignoreColumns;

    @EruptField(
            edit = @Edit(title = "Package", desc = "Written as the package statement of every generated class")
    )
    private String packageName;

    @EruptField(
            edit = @Edit(title = "Parent Class", notNull = true, type = EditType.CHOICE,
                    desc = "Columns the parent class already declares are skipped",
                    choiceType = @ChoiceType(fetchHandler = SuperModelChoice.class))
    )
    private SuperModel superClass = SuperModel.BASE_MODEL;

    @EruptField(
            edit = @Edit(title = "Overwrite Existing", notNull = true, type = EditType.BOOLEAN, boolType = @BoolType,
                    desc = "Replaces the definition previously imported from the same table")
    )
    private Boolean overwrite = false;

}
