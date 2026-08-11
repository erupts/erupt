package xyz.erupt.monitor.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.monitor.service.EruptFieldInfoDataService;

/**
 * Drill target of the erupt class registry: one row per @EruptField declaration,
 * showing the raw (untranslated) annotation metadata of each field.
 *
 * @author YuePeng
 */
@Erupt(
        name = "Erupt Field Info",
        power = @Power(add = false, edit = false, delete = false, export = true, viewDetails = false)
)
@EruptDataProcessor(EruptFieldInfoDataService.DATA_PROCESSOR)
@EruptI18n
@Getter
@Setter
public class EruptFieldInfo {

    @EruptField
    private String id;

    @EruptField(
            views = @View(title = "Class Name", sortable = true),
            edit = @Edit(title = "Class Name", search = @Search(operator = QueryExpression.LIKE))
    )
    private String eruptName;

    @EruptField(
            views = @View(title = "Field Name"),
            edit = @Edit(title = "Field Name", search = @Search(operator = QueryExpression.LIKE))
    )
    private String fieldName;

    @EruptField(
            views = @View(title = "Title"),
            edit = @Edit(title = "Title", search = @Search(operator = QueryExpression.LIKE))
    )
    private String title;

    @EruptField(
            views = @View(title = "Field Type"),
            edit = @Edit(title = "Field Type", search = @Search(operator = QueryExpression.LIKE))
    )
    private String fieldType;

    @EruptField(
            views = @View(title = "Edit Type", sortable = true),
            edit = @Edit(title = "Edit Type", search = @Search(operator = QueryExpression.LIKE))
    )
    private String editType;

    @EruptField(
            views = @View(title = "Required", type = ViewType.BOOLEAN),
            edit = @Edit(title = "Required", type = EditType.BOOLEAN, search = @Search)
    )
    private Boolean notNull;

    @EruptField(
            views = @View(title = "Searchable", type = ViewType.BOOLEAN),
            edit = @Edit(title = "Searchable", type = EditType.BOOLEAN, search = @Search)
    )
    private Boolean searchable;

}
