package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.annotation.model.Alert;
import xyz.erupt.annotation.model.Row;
import xyz.erupt.annotation.query.Condition;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public interface DataProxy<@Comment("Erupt object") MODEL> extends MetaProxy<MODEL> {

    @Comment("Validate")
    default void validate(MODEL model) throws EruptException {
    }

    @Comment("Before add")
    default void beforeAdd(MODEL model) {
    }

    @Comment("After add")
    default void afterAdd(MODEL model) {
    }

    @Comment("Before update")
    default void beforeUpdate(MODEL model) {
    }

    @Comment("After update")
    default void afterUpdate(MODEL model) {
    }

    @Comment("Before delete")
    default void beforeDelete(MODEL model) {
    }

    @Comment("After delete")
    default void afterDelete(MODEL model) {
    }

    @Comment("Before fetch; return value is a custom query condition")
    default String beforeFetch(List<Condition> conditions) {
        return null;
    }

    @Comment("Post-fetch result processing")
    default void afterFetch(@Comment("query result") Collection<Map<String, Object>> list) {
    }

    @Comment("Custom rendered HTML content; the returned HTML will be displayed in the view")
    default String extraContent(List<Condition> conditions, Collection<Map<String, Object>> list) {
        return null;
    }

    @Comment("Add data behavior; can be used for initialization and similar operations")
    default void addBehavior(MODEL model) {
    }

    @Comment("View data behavior; pre-processes the data to be viewed (read-only)")
    default void viewBehavior(MODEL model) {
        this.editBehavior(model);
    }

    @Comment("Edit data behavior; pre-processes the data to be edited")
    default void editBehavior(MODEL model) {
    }

    //model must be passed as a parameter, because dataProxy can have multi-level definitions and return values cannot propagate objects across multiple levels
    @Comment("Default search condition")
    default void searchCondition(Map<String, Object> condition) {

    }

    @Comment("Excel export; the parameter must be cast to a WorkBook object")
    default void excelExport(@Comment("POI document object") Object workbook) {
    }

    @Comment("Excel import; processes the POI object, the parameter must be cast to a WorkBook object")
    default void excelImport(@Comment("POI document object") Object workbook) {
    }

    @Comment("Excel import; processes the structured data extracted from the Excel file")
    default void excelImportProcess(@Comment("data object") List<MODEL> list) {
    }

    @Comment("Print Erupt object")
    default String print(MODEL model, String content) {
        return content;
    }

    @Comment("Alert prompt")
    default Alert alert(List<Condition> conditions) {
        return null;
    }


    @Comment("Custom rows; supports row-level computation and similar capabilities")
    default List<Row> extraRow(List<Condition> conditions) {
        return null;
    }


    @Comment("Form view: called before rendering. Load data into model from your own source.")
    default void formViewBehavior(MODEL model) {
    }

    @Comment("""
            Form view: called on save after field validation passes.
            Persist the model to your own data source.
            Throw EruptException to abort with a user-visible message.
            """)
    default void formSave(MODEL model) throws EruptException {
    }

}
