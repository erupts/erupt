package xyz.erupt.monitor.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.monitor.handler.EruptClassDataProcessorFetchHandler;
import xyz.erupt.monitor.handler.EruptClassPublishMenu;
import xyz.erupt.monitor.handler.EruptClassSourceFetchHandler;
import xyz.erupt.monitor.service.EruptClassInfoDataService;
import xyz.erupt.upms.model.input.MenuPublishModal;

/**
 * Read-only registry view over all @Erupt classes loaded in the current process,
 * fed from EruptCoreService memory rather than a database table.
 *
 * @author YuePeng
 */
@Erupt(
        name = "Erupt Class Registry",
        primaryKeyCol = "name",
        power = @Power(add = false, edit = false, delete = false, export = false),
        // Rows come from an in-memory scan of EruptCoreService (~dozens–hundreds); paginate client-side
        // to avoid rebuilding the full list on every page/sort click
        drills = @Drill(
                title = "Fields",
                link = @Link(column = "name", linkErupt = EruptFieldInfo.class, joinColumn = "eruptName")
        ),
        rowOperation = @RowOperation(
                title = "Publish to Menu",
                icon = "fa fa-paper-plane",
                mode = RowOperation.Mode.SINGLE,
                // table rows carry the boolType display text, not a boolean; the symbols below are
                // locale-stable (absent from i18n CSVs) so this comparison works in every language
                ifExpr = "item.published === '×'",
                eruptClass = MenuPublishModal.class,
                operationHandler = EruptClassPublishMenu.class
        )
)
@EruptDataProcessor(EruptClassInfoDataService.DATA_PROCESSOR)
@EruptI18n
@Getter
@Setter
public class EruptClassInfo {

    @EruptField(
            views = @View(title = "Source", sortable = true),
            edit = @Edit(title = "Source", type = EditType.CHOICE, search = @Search,
                    choiceType = @ChoiceType(fetchHandler = EruptClassSourceFetchHandler.class))
    )
    private String source;

    @EruptField(
            views = @View(title = "Class Name", sortable = true),
            edit = @Edit(title = "Class Name", search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Display Name"),
            edit = @Edit(title = "Display Name", search = @Search(operator = QueryExpression.LIKE))
    )
    private String displayName;

    @EruptField(
//            views = @View(title = "Full Class Name"),
            edit = @Edit(title = "Full Class Name", search = @Search(operator = QueryExpression.LIKE))
    )
    private String clazz;

    @EruptField(
            views = @View(title = "Multi-language", type = ViewType.BOOLEAN, sortable = true),
            edit = @Edit(title = "Multi-language", type = EditType.BOOLEAN, search = @Search)
    )
    private Boolean i18n;

    @EruptField(
            views = @View(title = "Field Count", sortable = true),
            edit = @Edit(title = "Field Count", type = EditType.NUMBER)
    )
    private Integer fieldCount;

    @EruptField(
            views = @View(title = "Data Processor"),
            edit = @Edit(title = "Data Processor", type = EditType.CHOICE, search = @Search,
                    choiceType = @ChoiceType(fetchHandler = EruptClassDataProcessorFetchHandler.class))
    )
    private String dataProcessor;

    @EruptField(
            views = @View(title = "Runtime Registered", type = ViewType.BOOLEAN),
            edit = @Edit(title = "Runtime Registered", type = EditType.BOOLEAN, search = @Search)
    )
    private Boolean runtime;

    @EruptField(
            views = @View(title = "Published", type = ViewType.BOOLEAN, sortable = true),
            edit = @Edit(title = "Published", type = EditType.BOOLEAN, search = @Search,
                    boolType = @BoolType(trueText = "✓", falseText = "×"))
    )
    private Boolean published;

    // Populated only by findDataById, so the class-level annotation JSON shows in the detail view without bloating list payloads
    @EruptField(
            edit = @Edit(title = "Model JSON", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "json", height = 500))
    )
    private String json;

}
