package xyz.erupt.cloud.server.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.erupt.cloud.server.service.TestContext;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.view.EruptBuildModel;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NodeRequestNormalizer}: the values a v2 node receives (PROTOCOL.md §12.4).
 *
 * @author YuePeng
 */
class NodeRequestNormalizerTest {

    // Article: numeric key, a reference to Category (numeric key) and to Tag (string key), every scalar type
    private static final EruptBuildModel ARTICLE = build("""
            {"eruptModel": {"eruptName": "Article",
              "eruptJson": {"primaryKeyCol": "id", "linkTree": {"field": "category", "dependNode": false}},
              "eruptFieldModels": [
                {"fieldName": "id", "fieldReturnName": "number", "eruptFieldJson": {"edit": {"title": "", "type": "NUMBER"}}},
                {"fieldName": "title", "fieldReturnName": "String", "eruptFieldJson": {"edit": {"title": "Title", "type": "INPUT",
                  "search": {"value": true, "operator": "LIKE"}}}},
                {"fieldName": "views", "fieldReturnName": "number", "eruptFieldJson": {"edit": {"title": "Views", "type": "NUMBER", "search": {"value": true}}}},
                {"fieldName": "top", "fieldReturnName": "Boolean", "eruptFieldJson": {"edit": {"title": "Top", "type": "BOOLEAN"}}},
                {"fieldName": "publishAt", "fieldReturnName": "Date", "eruptFieldJson": {"edit": {"title": "Publish", "type": "DATE",
                  "dateType": {"type": "DATE_TIME"}, "search": {"value": true}}}},
                {"fieldName": "day", "fieldReturnName": "Date", "eruptFieldJson": {"edit": {"title": "Day", "type": "DATE", "dateType": {"type": "DATE"}}}},
                {"fieldName": "month", "fieldReturnName": "String", "eruptFieldJson": {"edit": {"title": "Month", "type": "DATE", "dateType": {"type": "MONTH"}}}},
                {"fieldName": "category", "fieldReturnName": "Category", "eruptFieldJson": {"edit": {"title": "Category", "type": "REFERENCE_TABLE",
                  "search": {"value": true}}}},
                {"fieldName": "tag", "fieldReturnName": "Tag", "eruptFieldJson": {"edit": {"title": "Tag", "type": "REFERENCE_TREE",
                  "referenceTreeType": {"id": "code", "label": "name"}}}},
                {"fieldName": "readers", "fieldReturnName": "Reader", "eruptFieldJson": {"edit": {"title": "Readers", "type": "CHECKBOX"}}},
                {"fieldName": "labels", "fieldReturnName": "String", "eruptFieldJson": {"edit": {"title": "Labels", "type": "MULTI_CHOICE"}}},
                {"fieldName": "lines", "fieldReturnName": "Line", "eruptFieldJson": {"edit": {"title": "Lines", "type": "TAB_TABLE_ADD"}}}
              ]}}""");

    private static final EruptBuildModel CATEGORY = build("""
            {"eruptModel": {"eruptName": "Category", "eruptJson": {"primaryKeyCol": "id"}, "eruptFieldModels": [
              {"fieldName": "id", "fieldReturnName": "number", "eruptFieldJson": {"edit": {"title": "", "type": "NUMBER"}}}]}}""");

    private static final EruptBuildModel TAG = build("""
            {"eruptModel": {"eruptName": "Tag", "eruptJson": {"primaryKeyCol": "code"}, "eruptFieldModels": [
              {"fieldName": "code", "fieldReturnName": "String", "eruptFieldJson": {"edit": {"title": "", "type": "INPUT"}}}]}}""");

    private static final Function<String, EruptBuildModel> BUILDS = name -> {
        EruptBuildModel build = Map.of("Category", CATEGORY, "Tag", TAG).get(name);
        if (null == build) throw new IllegalStateException(name + " not served");
        return build;
    };

    @BeforeAll
    static void context() {
        TestContext.init();
    }

    private static EruptBuildModel build(String json) {
        return GsonFactory.getGson().fromJson(json, EruptBuildModel.class);
    }

    private static JsonObject json(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static JsonObject query(EruptBuildModel build, String json) {
        return NodeRequestNormalizer.query(build, json(json), BUILDS);
    }

    private static JsonArray conditions(JsonObject query) {
        return query.getAsJsonArray("condition");
    }

    @Test
    void recordValuesTakeTheTypeTheirFieldStores() {
        JsonObject record = NodeRequestNormalizer.record(ARTICLE, json("""
                {"id": "7", "title": 12, "views": "42", "top": "true", "publishAt": "2026-11-05T08:30",
                 "day": "2026-11-05 00:00:00", "month": "2026-11-05", "category": {"id": "3", "name": "News"},
                 "tag": {"code": "java", "name": "Java"}, "readers": [{"id": 1}, {"id": 2}], "category_name": "News"}"""), BUILDS);
        assertEquals(json("""
                {"id": 7, "title": "12", "views": 42, "top": true, "publishAt": "2026-11-05 08:30:00", "day": "2026-11-05",
                 "month": "2026-11", "category": 3, "tag": "java", "readers": [1, 2]}"""), record);
    }

    @Test
    void normalizingTwiceChangesNothing() {
        JsonObject once = NodeRequestNormalizer.record(ARTICLE, json("""
                {"id": "7", "views": "42", "publishAt": "2026-11-05 08:30:15.250", "category": {"id": "3"}}"""), BUILDS);
        assertEquals(once, NodeRequestNormalizer.record(ARTICLE, once, BUILDS));
        JsonObject query = query(ARTICLE, """
                {"pageIndex": 1, "pageSize": 10, "linkTreeVal": ["4"],
                 "condition": [{"key": "views", "expression": "RANGE", "value": ["1", "9"]}]}""");
        assertEquals(query, NodeRequestNormalizer.query(ARTICLE, query, BUILDS));
    }

    @Test
    void conditionsTakeTheShapeTheirOperatorTakes() {
        JsonObject query = query(ARTICLE, """
                {"pageIndex": 2, "pageSize": 20, "sort": [{"field": "views", "direction": "DESC"}], "vis": "board",
                 "condition": [
                   {"key": "title", "value": "50%"},
                   {"key": "views", "expression": "RANGE", "value": ["1", "9"]},
                   {"key": "category", "expression": "IN", "value": {"id": "3", "name": "News"}},
                   {"key": "publishAt", "expression": "NOT_NULL", "value": "ignored"},
                   {"key": "views", "expression": "EQ", "value": ""},
                   {"key": "category", "expression": "IN", "value": []}]}""");
        assertEquals(JsonParser.parseString("""
                [{"key": "title", "expression": "LIKE", "value": "50%"},
                 {"key": "views", "expression": "RANGE", "value": [1, 9]},
                 {"key": "category", "expression": "IN", "value": [3]},
                 {"key": "publishAt", "expression": "NOT_NULL"}]"""), conditions(query));
        // paging, sort and the view travel as they are
        assertEquals(2, query.get("pageIndex").getAsInt());
        assertEquals("board", query.get("vis").getAsString());
        assertEquals("DESC", query.getAsJsonArray("sort").get(0).getAsJsonObject().get("direction").getAsString());
    }

    @Test
    void anOperatorTheRequestLeavesOutIsTheFields() {
        JsonObject query = query(ARTICLE, """
                {"condition": [{"key": "publishAt", "value": ["2026-11-01", "2026-11-30 23:59:59"]}, {"key": "views", "value": "3"}]}""");
        assertEquals("RANGE", conditions(query).get(0).getAsJsonObject().get("expression").getAsString());
        assertEquals(JsonParser.parseString("[\"2026-11-01 00:00:00\", \"2026-11-30 23:59:59\"]"),
                conditions(query).get(0).getAsJsonObject().get("value"));
        assertEquals("EQ", conditions(query).get(1).getAsJsonObject().get("expression").getAsString());
    }

    @Test
    void aLinkTreePickIsAnInCondition() {
        JsonObject query = query(ARTICLE, """
                {"linkTreeVal": ["4", "5"], "condition": []}""");
        assertFalse(query.has("linkTreeVal"));
        assertEquals(json("""
                {"key": "category", "expression": "IN", "value": [4, 5]}"""), conditions(query).get(0));
    }

    @Test
    void aDependentLinkTreeWithNothingPickedMatchesNoRow() {
        EruptBuildModel dependent = build(GsonFactory.getGson().toJson(ARTICLE));
        dependent.getEruptModel().getEruptJson().getAsJsonObject("linkTree").addProperty("dependNode", true);
        JsonObject query = NodeRequestNormalizer.query(dependent, json("{}"), BUILDS);
        assertEquals(json("""
                {"key": "id", "expression": "NULL"}"""), conditions(query).get(0));
        assertEquals(1, conditions(NodeRequestNormalizer.query(dependent, query, BUILDS)).size());
    }

    @Test
    void aValueThatDoesNotFitItsFieldIsRefused() {
        assertThrows(EruptApiErrorTip.class, () -> NodeRequestNormalizer.record(ARTICLE, json("{\"views\": \"many\"}"), BUILDS));
        assertThrows(EruptApiErrorTip.class, () -> NodeRequestNormalizer.record(ARTICLE, json("{\"day\": \"05/11/2026\"}"), BUILDS));
        assertThrows(EruptApiErrorTip.class, () -> NodeRequestNormalizer.record(ARTICLE, json("{\"publishAt\": \"2026-11-05T08:30:00Z\"}"), BUILDS));
        assertThrows(EruptApiErrorTip.class, () -> NodeRequestNormalizer.record(ARTICLE, json("{\"top\": \"yes\"}"), BUILDS));
        assertThrows(EruptApiErrorTip.class, () -> NodeRequestNormalizer.record(ARTICLE, json("{\"id\": \"seven\"}"), BUILDS));
        assertThrows(EruptApiErrorTip.class, () -> query(ARTICLE, "{\"condition\": [{\"key\": \"views\", \"expression\": \"RANGE\", \"value\": [1]}]}"));
        assertThrows(EruptApiErrorTip.class, () -> query(ARTICLE, "{\"condition\": [{\"key\": \"views\", \"expression\": \"SOUNDS_LIKE\", \"value\": 1}]}"));
    }

    @Test
    void aReferenceToAnEruptTheNodeDoesNotServeKeepsItsKey() {
        EruptBuildModel orphan = build("""
                {"eruptModel": {"eruptName": "Note", "eruptJson": {}, "eruptFieldModels": [
                  {"fieldName": "owner", "fieldReturnName": "Ghost", "eruptFieldJson": {"edit": {"title": "Owner", "type": "REFERENCE_TABLE"}}}]}}""");
        assertEquals(json("{\"owner\": \"9\"}"), NodeRequestNormalizer.record(orphan, json("{\"owner\": {\"id\": \"9\"}}"), BUILDS));
    }

    @Test
    void aValueANodeCouldReadAsAQueryIsRefused() {
        // an object or a list where one value goes, in a condition or a record: never passed on to a node's query
        assertThrows(EruptApiErrorTip.class, () -> query(ARTICLE, """
                {"condition": [{"key": "views", "expression": "EQ", "value": {"$gt": 0}}]}"""));
        assertThrows(EruptApiErrorTip.class, () -> query(ARTICLE, """
                {"condition": [{"key": "title", "expression": "LIKE", "value": {"$regex": ".*"}}]}"""));
        assertThrows(EruptApiErrorTip.class, () -> query(ARTICLE, """
                {"condition": [{"key": "views", "expression": "IN", "value": [1, {"$gt": 0}]}]}"""));
        assertThrows(EruptApiErrorTip.class, () -> NodeRequestNormalizer.record(ARTICLE, json("{\"title\": {\"$ne\": null}}"), BUILDS));
        assertThrows(EruptApiErrorTip.class, () -> NodeRequestNormalizer.record(ARTICLE, json("{\"title\": [\"a\"]}"), BUILDS));
        assertThrows(EruptApiErrorTip.class, () -> NodeRequestNormalizer.record(ARTICLE, json("{\"labels\": [\"a\", {\"b\": 1}]}"), BUILDS));
        // the types that hold lists keep them
        JsonObject record = NodeRequestNormalizer.record(ARTICLE, json("""
                {"labels": ["a", "b"], "lines": [{"sku": "x", "qty": 2}]}"""), BUILDS);
        assertEquals(JsonParser.parseString("[\"a\", \"b\"]"), record.get("labels"));
        assertEquals(JsonParser.parseString("[{\"sku\": \"x\", \"qty\": 2}]"), record.get("lines"));
    }

    @Test
    void aQueryCarriesOnlyWhatANodeReads() {
        JsonObject query = query(ARTICLE, """
                {"pageIndex": 2, "pageSize": 10, "vis": "board", "linkTreeVal": [], "filter": "1=1", "drill": {"erupt": "X", "code": "y"}}""");
        assertEquals(Set.of("pageIndex", "pageSize", "vis", "drill", "condition"), query.keySet());
    }

}
