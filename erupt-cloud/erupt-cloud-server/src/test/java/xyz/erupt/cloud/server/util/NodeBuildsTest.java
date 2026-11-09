package xyz.erupt.cloud.server.util;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import xyz.erupt.cloud.common.node.CloudProtocol;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.view.EruptBuildModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NodeBuilds}: a v2 node's build, declared sparsely, made whole and checked (PROTOCOL.md §12.3).
 *
 * @author YuePeng
 */
class NodeBuildsTest {

    private static EruptBuildModel build(String json) {
        return GsonFactory.getGson().fromJson(json, EruptBuildModel.class);
    }

    private static final String SPARSE = """
            {"eruptModel": {"eruptName": "Article",
              "eruptJson": {"name": "Article", "power": {"delete": false},
                "drills": [{"code": "comments", "title": "Comments",
                  "link": {"linkErupt": "Comment", "column": "id", "joinColumn": "article"}}],
                "rowOperation": [{"code": "publish", "title": "Publish"}]},
              "eruptFieldModels": [
                {"fieldName": "title", "fieldReturnName": "String", "eruptFieldJson": {"edit": {"title": "Title", "type": "INPUT"},
                  "params": {"width": {"value": "200"}}}}]},
             "power": {"delete": false}}""";

    @Test
    void aSparseBuildGetsEveryDefault() {
        EruptBuildModel build = build(SPARSE);
        NodeBuilds.complete(build, CloudProtocol.V2);
        JsonObject erupt = build.getEruptModel().getEruptJson();
        // declared values kept, the rest defaulted, nested and in arrays
        assertEquals("Article", erupt.get("name").getAsString());
        assertEquals("id", erupt.get("primaryKeyCol").getAsString());
        assertTrue(erupt.has("layout"));
        JsonObject operation = erupt.getAsJsonArray("rowOperation").get(0).getAsJsonObject();
        assertEquals("publish", operation.get("code").getAsString());
        assertTrue(operation.has("mode"));
        JsonObject edit = build.getEruptModel().getEruptFieldModels().get(0).getEruptFieldJson().getAsJsonObject("edit");
        assertEquals("Title", edit.get("title").getAsString());
        assertTrue(edit.has("notNull"));
        assertTrue(edit.has("inputType"));
        // a @ToMap attribute declared as the object it travels as
        assertEquals("200", build.getEruptModel().getEruptFieldModels().get(0).getEruptFieldJson()
                .getAsJsonObject("params").getAsJsonObject("width").get("value").getAsString());
        // the build's power: what the node switched off, true for the rest
        assertFalse(build.getPower().isDelete());
        assertTrue(build.getPower().isAdd());
    }

    @Test
    void anAutoViewIsResolvedLikeALocalOne() {
        EruptBuildModel build = build("""
                {"eruptModel": {"eruptName": "Article", "eruptJson": {}, "eruptFieldModels": [
                  {"fieldName": "title", "fieldReturnName": "String", "eruptFieldJson": {"views": [{"title": "Title"}],
                    "edit": {"title": "Title", "type": "INPUT"}}},
                  {"fieldName": "views", "fieldReturnName": "number", "eruptFieldJson": {"views": [{"title": "Views"}],
                    "edit": {"title": "Views", "type": "NUMBER"}}},
                  {"fieldName": "featured", "fieldReturnName": "Boolean", "eruptFieldJson": {"views": [{"title": "Featured"}],
                    "edit": {"title": "Featured", "type": "BOOLEAN"}}},
                  {"fieldName": "reviewAt", "fieldReturnName": "Date", "eruptFieldJson": {"views": [{"title": "Review"}],
                    "edit": {"title": "Review", "type": "DATE", "dateType": {"type": "DATE_TIME"}}}},
                  {"fieldName": "tags", "fieldReturnName": "String", "eruptFieldJson": {"views": [{"title": "Tags"}],
                    "edit": {"title": "Tags", "type": "MULTI_CHOICE"}}},
                  {"fieldName": "raw", "fieldReturnName": "number", "eruptFieldJson": {"views": [{"title": "Raw", "type": "TEXT"}],
                    "edit": {"title": "Raw", "type": "NUMBER"}}}]}}""");
        NodeBuilds.complete(build, CloudProtocol.V2);
        List<String> types = build.getEruptModel().getEruptFieldModels().stream()
                .map(f -> f.getEruptFieldJson().getAsJsonArray("views").get(0).getAsJsonObject().get("type").getAsString()).toList();
        // by the edit first (ViewProxy), then by the stored type; a declared type stays
        assertEquals(List.of("TEXT", "NUMBER", "BOOLEAN", "DATE_TIME", "TAB_VIEW", "TEXT"), types);
    }

    @Test
    void anAutoSearchOperatorAndBoolTypeAreResolvedLikeALocalOne() {
        EruptBuildModel build = build("""
                {"eruptModel": {"eruptName": "Article", "eruptJson": {}, "eruptFieldModels": [
                  {"fieldName": "title", "fieldReturnName": "String", "eruptFieldJson": {
                    "edit": {"title": "Title", "type": "INPUT", "search": {"value": true}}}},
                  {"fieldName": "day", "fieldReturnName": "Date", "eruptFieldJson": {
                    "edit": {"title": "Day", "type": "DATE", "dateType": {"type": "DATE"}, "search": {"value": true}}}},
                  {"fieldName": "since", "fieldReturnName": "Date", "eruptFieldJson": {
                    "edit": {"title": "Since", "type": "DATE", "search": {"value": true, "operator": "GTE"}}}},
                  {"fieldName": "optional", "fieldReturnName": "Boolean", "eruptFieldJson": {
                    "edit": {"title": "Optional", "type": "BOOLEAN"}}},
                  {"fieldName": "required", "fieldReturnName": "Boolean", "eruptFieldJson": {
                    "edit": {"title": "Required", "type": "BOOLEAN", "notNull": true}}}]}}""");
        NodeBuilds.complete(build, CloudProtocol.V2);
        List<JsonObject> edits = build.getEruptModel().getEruptFieldModels().stream()
                .map(f -> f.getEruptFieldJson().getAsJsonObject("edit")).toList();
        // SearchProxy: a range for dates, an exact match otherwise; a declared operator stays
        assertEquals(List.of("EQ", "RANGE", "GTE"), edits.subList(0, 3).stream()
                .map(e -> e.getAsJsonObject("search").get("operator").getAsString()).toList());
        // BoolTypeProxy: radios while the value may be left unset, a switch once it may not
        assertEquals(List.of("RADIO", "SWITCH"), edits.subList(3, 5).stream()
                .map(e -> e.getAsJsonObject("boolType").get("type").getAsString()).toList());
    }

    @Test
    void aValueThatDoesNotReadBackIsRefusedByName() {
        EruptBuildModel build = build(SPARSE.replace("\"type\": \"INPUT\"", "\"type\": \"TEXTFIELD\""));
        EruptWebApiRuntimeException e = assertThrows(EruptWebApiRuntimeException.class, () -> NodeBuilds.complete(build, CloudProtocol.V2));
        assertTrue(e.getMessage().contains("Article.title"), e.getMessage());
        assertTrue(e.getMessage().contains("TEXTFIELD"), e.getMessage());
        // a v1 node keeps the quiet completion of its fields
        assertDoesNotThrow(() -> NodeBuilds.complete(build(SPARSE.replace("\"type\": \"INPUT\"", "\"type\": \"TEXTFIELD\"")), CloudProtocol.V1));
    }

    @Test
    void whatTheServerAloneUsesIsHiddenFromTheBrowser() {
        EruptBuildModel build = build(SPARSE);
        NodeBuilds.complete(build, CloudProtocol.V2);
        NodeBuilds.hideServerOnly(build);
        JsonObject link = NodeBuilds.links(build.getEruptModel()).get(0);
        assertEquals("Comment", link.get("linkErupt").getAsString());
        assertFalse(link.has("column"));
        assertFalse(link.has("joinColumn"));
    }

}
