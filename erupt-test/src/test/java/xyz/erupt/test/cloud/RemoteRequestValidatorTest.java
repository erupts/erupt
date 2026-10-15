package xyz.erupt.test.cloud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import xyz.erupt.core.util.RemoteRequestValidator;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.R;
import xyz.erupt.test.EruptApplicationTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RemoteRequestValidatorTest extends EruptApplicationTests {

    // the shape of a node's EruptBuildModel, reduced to what the validator reads
    private static final EruptBuildModel BUILD = GsonFactory.getGson().fromJson("""
            {"eruptModel": {"eruptName": "Article",
              "eruptJson": {"primaryKeyCol": "id", "layout": {"pageSizes": [10, 20, 2000]}},
              "eruptFieldModels": [
                {"fieldName": "id", "fieldReturnName": "number", "eruptFieldJson": {"views": [],
                  "edit": {"title": "", "type": "NUMBER", "notNull": false, "search": {"value": false}}}},
                {"fieldName": "title", "fieldReturnName": "String", "eruptFieldJson": {"views": [{"title": "Title", "column": "", "sortable": true}],
                  "edit": {"title": "Title", "type": "INPUT", "notNull": true, "search": {"value": true}}}},
                {"fieldName": "body", "fieldReturnName": "String", "eruptFieldJson": {"views": [],
                  "edit": {"title": "Body", "type": "TEXTAREA", "notNull": false, "search": {"value": false}, "textareaType": {"length": 5}}}},
                {"fieldName": "views", "fieldReturnName": "number", "eruptFieldJson": {"views": [{"title": "Views", "column": "", "sortable": false}],
                  "edit": {"title": "Views", "type": "NUMBER", "notNull": false, "search": {"value": false}, "numberType": {"min": 0, "max": 100}}}},
                {"fieldName": "code", "fieldReturnName": "String", "eruptFieldJson": {"views": [],
                  "edit": {"title": "Code", "type": "INPUT", "notNull": false, "search": {"value": false}, "inputType": {"length": 4}}}},
                {"fieldName": "status", "fieldReturnName": "String", "eruptFieldJson": {"views": [{"title": "Status", "column": "", "sortable": false}],
                  "edit": {"title": "Status", "type": "CHOICE", "notNull": false, "search": {"value": true}}},
                  "componentValue": [{"value": "draft", "label": "Draft"}, {"value": "published", "label": "Published"}]},
                {"fieldName": "category", "fieldReturnName": "Category", "eruptFieldJson": {"views": [{"title": "Category", "column": "name", "sortable": true}],
                  "edit": {"title": "Category", "type": "REFERENCE_TABLE", "notNull": false, "search": {"value": true}}}}
              ]}}""", EruptBuildModel.class);

    private static String query(String json) {
        return RemoteRequestValidator.validateQuery(BUILD, JsonParser.parseString(json).getAsJsonObject());
    }

    private static R<?> record(String json) {
        return RemoteRequestValidator.validateRecord(BUILD, JsonParser.parseString(json).getAsJsonObject());
    }

    @Test
    void queries() {
        String illegalCondition = I18nTranslate.$translate("Illegal search condition");
        String illegalSort = I18nTranslate.$translate("Illegal sort field");
        assertNull(query("""
                {"pageIndex": 1, "pageSize": 20, "condition": [{"key": "title", "expression": "LIKE", "value": "a"},
                 {"key": "status", "value": "draft"}, {"key": "category", "expression": "IN", "value": [{"id": 1}]}],
                 "sort": [{"field": "title", "direction": "DESC"}, {"field": "category.name"}, {"field": "id"}]}"""));
        assertEquals(illegalCondition, query("{\"condition\": [{\"key\": \"views\", \"value\": 1}]}"), "not searchable");
        assertEquals(illegalCondition, query("{\"condition\": [{\"key\": \"title\\\" OR 1=1\", \"value\": 1}]}"), "unknown key");
        assertEquals(illegalCondition, query("{\"condition\": [{\"key\": \"title\", \"expression\": \"DROP\"}]}"), "unknown operator");
        assertEquals(illegalCondition, query("{\"condition\": [{\"key\": \"title\", \"expression\": \"AUTO\"}]}"), "AUTO is never a real operator");
        assertEquals(illegalCondition, query("{\"condition\": [{\"key\": \"category\", \"expression\": \"LIKE\", \"value\": \"x\"}]}"), "text match on a reference");
        assertEquals(illegalCondition, query("{\"condition\": [\"title\"]}"), "not an object");
        assertEquals(illegalSort, query("{\"sort\": [{\"field\": \"views\"}]}"), "column not sortable");
        assertEquals(illegalSort, query("{\"sort\": [{\"field\": \"body\"}]}"), "not in the table");
        assertEquals(illegalSort, query("{\"sort\": [{\"field\": \"category.secret\"}]}"), "another column of the reference");
        assertNull(query("{\"pageSize\": 2000}"), "a page size the model offers");
        assertEquals(I18nTranslate.$translate("Page size is too large"), query("{\"pageSize\": 2001}"));
    }

    @Test
    void records() {
        assertNull(record("{\"title\": \"Hello\", \"body\": \"short\", \"views\": \"3\", \"status\": \"draft\"}"));
        String notNull = "Title " + I18nTranslate.$translate("erupt.notnull");
        assertEquals(notNull, record("{\"status\": \"draft\"}").getMessage(), "missing");
        assertEquals(notNull, record("{\"title\": \"  \"}").getMessage(), "blank");
        assertEquals("Views " + I18nTranslate.$translate("erupt.must.number"), record("{\"title\": \"t\", \"views\": \"many\"}").getMessage());
        assertEquals("Body " + I18nTranslate.$translate("erupt.data.limit_length"), record("{\"title\": \"t\", \"body\": \"toolong\"}").getMessage());
        // the declared limits come from EruptValueValidator, reading the edit JSON as the annotation
        assertEquals("Status " + I18nTranslate.$translate("erupt.choice.option_invalid") + " → deleted",
                record("{\"title\": \"t\", \"status\": \"deleted\"}").getMessage());
        assertEquals("Views " + I18nTranslate.$translate("erupt.value.out_of_range") + " → 101", record("{\"title\": \"t\", \"views\": 101}").getMessage());
        assertEquals("Code " + I18nTranslate.$translate("erupt.data.limit_length") + " → 5 > 4", record("{\"title\": \"t\", \"code\": \"ABCDE\"}").getMessage());
        assertNull(record("{\"title\": \"t\", \"views\": 100, \"code\": \"ABCD\", \"status\": \"published\"}"));
        R<?> xss = record("{\"title\": \"<script>alert(1)</script>\"}");
        assertEquals("Title " + I18nTranslate.$translate("erupt.attack.xss"), xss.getMessage());
        assertEquals(R.PromptWay.DIALOG, xss.getPromptWay());
    }

}
