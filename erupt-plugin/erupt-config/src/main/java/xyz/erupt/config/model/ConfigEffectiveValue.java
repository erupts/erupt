package xyz.erupt.config.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.config.handler.ConfigRefreshHandler;
import xyz.erupt.config.service.ConfigEffectiveDataService;
import xyz.erupt.core.annotation.EruptDataProcessor;

/**
 * One running property of this instance: the merged result of the layered profiles, with the
 * profile it came from. Read only; what the application sees through {@code @Value} right now.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Erupt(name = "Effective values", power = @Power(add = false, edit = false, delete = false, viewDetails = false, export = false),
        rowOperation = @RowOperation(code = "refresh", title = "Refresh now", icon = "fa fa-refresh", mode = RowOperation.Mode.BUTTON, callHint = "",
                tip = "Reloads the values from the database on every instance", operationHandler = ConfigRefreshHandler.class))
@EruptDataProcessor(ConfigEffectiveDataService.DATA_PROCESSOR)
public class ConfigEffectiveValue {

    @EruptField
    private String id;

    @EruptField(views = @View(title = "Key", sortable = true), edit = @Edit(title = "Key", search = @Search(operator = QueryExpression.LIKE)))
    private String key;

    @EruptField(views = @View(title = "Value", type = ViewType.TEXT), edit = @Edit(title = "Value", search = @Search(operator = QueryExpression.LIKE)))
    private String value;

    @EruptField(views = @View(title = "Source", sortable = true), edit = @Edit(title = "Source", search = @Search(operator = QueryExpression.LIKE),
            desc = "The profile that set the value; a later layer wins over default"))
    private String source;

    @EruptField(views = @View(title = "Type"), edit = @Edit(title = "Type", search = @Search))
    private String type;

}
