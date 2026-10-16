package xyz.erupt.cloud.node.lite;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.cloud.node.lite.Models.Article;
import xyz.erupt.cloud.node.lite.Models.Category;
import xyz.erupt.cloud.node.lite.Models.Note;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The node through its framework-neutral handler, against an in-process fake of the server's erupt-power. */
abstract class NodeTest {

    static final String TOKEN = "node-secret";
    static HttpServer server;
    static final Map<String, Boolean> REMOTE = new HashMap<>();

    Node node;

    abstract Repository<Article> articles();

    @BeforeAll
    static void fakeServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/erupt-cloud-api/erupt-power", ex -> {
            byte[] body = (ex.getRequestHeaders().getFirst("token").equals("down") ? "x" : Json.write(REMOTE)).getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(ex.getRequestHeaders().getFirst("token").equals("down") ? 500 : 200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @BeforeEach
    void setUp() {
        for (String r : List.of("add", "edit", "delete", "viewDetails", "export", "importable")) REMOTE.put(r, true);
        Models.CALLS.clear();
        node = new Node("blog", TOKEN, "http://127.0.0.1:" + server.getAddress().getPort())
                .register(Category.class, new MemoryRepository<>(Category.of("Tech", null), Category.of("Python", 1L), Category.of("Life", null)))
                .register(Article.class, articles())
                .register(Note.class, new MemoryRepository<>());
    }

    record Answer(int status, Object body) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map() {
            return (Map<String, Object>) body;
        }
    }

    Answer call(String method, String path, Object body, String erupt, String account, Map<String, String> headers) {
        Map<String, String> h = new LinkedHashMap<>();
        h.put("access-token", TOKEN);
        h.put("token", "t1");
        h.put("user", Base64.getEncoder().encodeToString(("{\"uid\":1,\"account\":\"" + account + "\"}").getBytes(StandardCharsets.UTF_8)));
        if (erupt != null) h.put("erupt", erupt);
        headers.forEach((k, v) -> h.put(k.toLowerCase(), v));
        byte[] raw = body == null ? new byte[0] : (body instanceof byte[] b ? b : Json.write(body).getBytes(StandardCharsets.UTF_8));
        Node.Response r = node.handle(new Node.Request(method, path, k -> h.get(k.toLowerCase()), raw));
        String text = new String(r.body(), StandardCharsets.UTF_8);
        return new Answer(r.status(), text.startsWith("{") || text.startsWith("[") ? Json.parse(text) : text);
    }

    Answer call(String method, String path, Object body) {
        return call(method, path, body, "Article", "admin", Map.of());
    }

    void ok(Answer a) {
        assertEquals(200, a.status(), String.valueOf(a.body()));
        assertEquals(true, a.map().get("success"), String.valueOf(a.body()));
    }

    void fail(Answer a) {
        assertEquals(200, a.status(), String.valueOf(a.body()));
        assertEquals(false, a.map().get("success"), String.valueOf(a.body()));
    }

    void add(Map<String, Object> row) {
        ok(call("POST", "/erupt-api/data/modify/Article", row));
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> rows(List<Map<String, Object>> condition, List<Map<String, Object>> sort) {
        Answer a = call("POST", "/erupt-api/data/table/Article", Map.of("pageIndex", 1, "pageSize", 10, "condition", condition, "sort", sort));
        assertEquals(200, a.status(), String.valueOf(a.body()));
        return (List<Map<String, Object>>) a.map().get("list");
    }

    List<Object> titles(List<Map<String, Object>> condition) {
        return rows(condition, List.of()).stream().map(r -> r.get("title")).toList();
    }

    static Map<String, Object> cond(String key, String op, Object value) {
        Map<String, Object> c = new HashMap<>();
        c.put("key", key);
        c.put("expression", op);
        c.put("value", value);
        return c;
    }

    @Test
    @SuppressWarnings("unchecked")
    void build() {
        Answer a = call("GET", "/erupt-api/build/Article", null);
        assertEquals(200, a.status());
        Map<String, Object> model = (Map<String, Object>) a.map().get("eruptModel");
        Map<String, Object> erupt = (Map<String, Object>) model.get("eruptJson");
        assertFalse(erupt.containsKey("name"), "@Transient name");
        assertFalse(erupt.containsKey("orderBy"), "@Transient orderBy");
        Map<String, Map<String, Object>> fields = new LinkedHashMap<>();
        for (Object f : (List<Object>) model.get("eruptFieldModels")) fields.put((String) ((Map<String, Object>) f).get("fieldName"), (Map<String, Object>) f);
        assertEquals(List.of("id", "title", "status", "level", "views", "featured", "day", "at", "stamp", "category", "topic"), List.copyOf(fields.keySet()));
        Map<String, Object> types = new LinkedHashMap<>();
        fields.forEach((k, f) -> types.put(k, ((Map<String, Object>) ((Map<String, Object>) f.get("eruptFieldJson")).get("edit")).get("type")));
        assertEquals("NUMBER", types.get("id"));
        assertEquals("CHOICE", types.get("level"), "an enum member is a CHOICE");
        assertEquals("REFERENCE_TABLE", types.get("category"));
        assertEquals("REFERENCE_TREE", types.get("topic"));
        assertEquals("number", fields.get("views").get("fieldReturnName"));
        assertEquals("Category", fields.get("category").get("fieldReturnName"));
        assertEquals(List.of("LOW", "HIGH"), ((List<Map<String, Object>>) fields.get("level").get("componentValue")).stream().map(o -> o.get("value")).toList());
        Map<String, Object> edit = (Map<String, Object>) ((Map<String, Object>) fields.get("title").get("eruptFieldJson")).get("edit");
        assertTrue(edit.containsKey("inputType"), "@Match: inputType for INPUT");
        assertFalse(edit.containsKey("numberType"), "@Match: no numberType for INPUT");
        assertEquals("EQ", ((Map<String, Object>) edit.get("search")).get("operator"));
        List<Map<String, Object>> ops = (List<Map<String, Object>>) erupt.get("rowOperation");
        assertEquals(List.of("publish", "feature"), ops.stream().map(o -> o.get("code")).toList(), "rebuild is shown to root only");
        assertEquals("PublishForm", ops.get(0).get("eruptClass"));
        assertEquals("Confirm whether to perform this operation", ops.get(0).get("callHint"));
        assertEquals("", ops.get(1).get("callHint"));
        assertEquals(List.of("publish"), List.copyOf(((Map<String, Object>) a.map().get("operationErupts")).keySet()));
        assertEquals(false, ((Map<String, Object>) a.map().get("power")).get("print"), "unsupported, hidden");
        assertEquals(true, ((Map<String, Object>) a.map().get("power")).get("cellEdit"), "done by the server");
        a = call("GET", "/erupt-api/build/Article", null, "Article", "root", Map.of());
        assertEquals(3, ((List<Object>) ((Map<String, Object>) ((Map<String, Object>) a.map().get("eruptModel")).get("eruptJson")).get("rowOperation")).size());
    }

    @Test
    void guards() {
        assertEquals(403, call("GET", "/erupt-api/build/Article", null, "Article", "admin", Map.of("Origin", "http://evil")).status());
        assertEquals(403, call("GET", "/erupt-api/build/Article", null, "Article", "admin", Map.of("Access-Token", "bad")).status());
        assertEquals(404, call("GET", "/erupt-api/build/Missing", null, "Missing", "admin", Map.of()).status());
        assertEquals(404, call("GET", "/erupt-api/build/Article", null, "Note", "admin", Map.of()).status());
        assertEquals(404, call("GET", "/erupt-api/build/Article", null, "Article", "admin", Map.of("eruptParent", "blog.Note")).status());
        assertEquals(200, call("GET", "/erupt-api/build/Article", null, "Article", "admin", Map.of("eruptParent", "blog.Article")).status());
        Answer health = call("GET", "/node-health", null, null, "admin", Map.of("Access-Token", ""));
        assertEquals(new Answer(200, "blog"), health);
    }

    @Test
    void signed() {
        String user = Base64.getEncoder().encodeToString("{\"uid\":1,\"account\":\"admin\"}".getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis() / 1000;
        String sig = CloudSignature.sign(TOKEN, "GET", "/erupt-api/build/Article?x=1", now, "power", user, new byte[0]);
        Map<String, String> h = Map.of("Access-Token", "", "Erupt-Signature", sig, "Erupt-Timestamp", String.valueOf(now), "Erupt-Enforced", "power", "token", "down");
        assertEquals(200, call("GET", "/erupt-api/build/Article?x=1", null, "Article", "admin", h).status(), "enforced power skips the callback");
        assertEquals(403, call("GET", "/erupt-api/build/Article?x=2", null, "Article", "admin", h).status(), "signed another target");
    }

    @Test
    void power() {
        REMOTE.put("add", false);
        assertEquals(403, call("POST", "/erupt-api/data/modify/Article", Map.of("title", "x")).status());
        assertEquals(403, call("POST", "/erupt-api/data/modify/Note/delete", List.of(1), "Note", "admin", Map.of()).status(), "declared delete off");
        assertEquals(403, call("POST", "/erupt-api/data/modify/Note", Map.of("text", "x"), "Note", "admin", Map.of("token", "down")).status(), "fails closed");
    }

    @Test
    @SuppressWarnings("unchecked")
    void crud() {
        add(Map.of("title", "Hello erupt", "status", "published", "level", "HIGH", "views", 10, "featured", true, "day", "2026-01-05", "at", "2026-01-05 10:30:00"));
        add(Map.of("title", "Second", "status", "draft", "views", "3", "featured", false, "day", "2026-03-01"));
        List<Map<String, Object>> all = rows(List.of(), List.of());
        assertEquals(List.of("Second", "Hello erupt"), all.stream().map(r -> r.get("title")).toList(), "orderBy id desc");
        Map<String, Object> first = all.get(1);
        assertEquals("2026-01-05", first.get("day"));
        assertEquals("2026-01-05T10:30:00.000", first.get("at"));
        assertEquals(10L, first.get("views"));
        assertEquals("HIGH", first.get("level"));
        assertEquals("added", first.get("stamp"), "DataProxy.beforeAdd");
        assertEquals(List.of("Hello erupt"), titles(List.of(cond("title", "LIKE", "erupt"))));
        assertEquals(List.of("Second"), titles(List.of(cond("status", "EQ", "draft"))));
        assertEquals(List.of("Hello erupt"), titles(List.of(cond("level", "EQ", "HIGH"))));
        assertEquals(List.of("Hello erupt"), titles(List.of(cond("featured", "EQ", true))));
        assertEquals(List.of("Second"), titles(List.of(cond("day", "RANGE", List.of("2026-02-01", "2026-12-31")))));
        assertEquals(List.of("Second", "Hello erupt"), rows(List.of(), List.of(Map.of("field", "views", "direction", "ASC"))).stream().map(r -> r.get("title")).toList());
        Object pk = first.get("id");
        Answer got = call("GET", "/erupt-api/data/Article/" + pk, null);
        Map<String, Object> row = new LinkedHashMap<>(got.map());
        row.put("title", "Renamed");
        ok(call("POST", "/erupt-api/data/modify/Article/update", row));
        Map<String, Object> updated = call("GET", "/erupt-api/data/Article/" + pk, null).map();
        assertEquals("Renamed", updated.get("title"));
        assertEquals("updated", updated.get("stamp"));
        ok(call("POST", "/erupt-api/data/modify/Article/delete", List.of(pk)));
        assertEquals(1, rows(List.of(), List.of()).size());
        assertEquals(200, call("GET", "/erupt-api/data/init-value/Article", null).status());
    }

    @Test
    @SuppressWarnings("unchecked")
    void references() {
        Answer b = call("GET", "/erupt-api/build/Article/category", null);
        assertEquals("Category", ((Map<String, Object>) b.map().get("eruptModel")).get("eruptName"));
        assertEquals(404, call("GET", "/erupt-api/build/Article/title", null).status());
        Answer picker = call("POST", "/erupt-api/data/Article/reference-table/category", Map.of("condition", List.of(cond("name", "LIKE", "py"))));
        assertEquals(List.of(Map.of("id", 2L, "name", "Python", "parent", 1L)), picker.map().get("list"));
        assertEquals(404, call("POST", "/erupt-api/data/Article/reference-table/topic", Map.of()).status());
        add(Map.of("title", "Ref", "category", Map.of("id", "2", "name", "Python"), "topic", Map.of("id", 1, "name", "Tech")));
        Map<String, Object> row = rows(List.of(), List.of()).get(0);
        assertEquals(2L, row.get("category"), "a reference travels as its key");
        assertEquals(1L, row.get("topic"));
        assertEquals(1, titles(List.of(cond("category", "EQ", Map.of("id", 2)))).size());
        assertEquals(0, titles(List.of(cond("category", "IN", List.of(Map.of("id", 1), Map.of("id", 3))))).size());
        fail(call("POST", "/erupt-api/data/table/Article", Map.of("condition", List.of(cond("category", "LIKE", "x")))));
        assertEquals(404, call("GET", "/erupt-api/data/Article/reference-tree/topic", null).status());
    }

    @Test
    void operations() {
        add(Map.of("title", "A"));
        add(Map.of("title", "B"));
        List<Object> ids = rows(List.of(), List.of()).stream().map(r -> r.get("id")).toList();
        String path = "/erupt-api/data/Article/operator/";
        fail(call("POST", path + "publish", Map.of("ids", ids, "param", Map.of())));
        Answer r = call("POST", path + "publish", Map.of("ids", ids, "param", Map.of("note", "go")));
        ok(r);
        assertEquals("Executed successfully", r.map().get("message"));
        assertEquals(List.of("publish 2 go"), Models.CALLS);
        assertEquals(List.of("published", "published"), rows(List.of(), List.of()).stream().map(x -> x.get("status")).toList());
        fail(call("POST", path + "feature", Map.of("ids", ids)));
        assertEquals("alert('featured')", call("POST", path + "feature", Map.of("ids", ids.subList(0, 1))).map().get("data"));
        fail(call("POST", path + "publish", Map.of("ids", List.of(), "param", Map.of("note", "go"))));
        assertEquals(403, call("POST", path + "rebuild", Map.of()).status());
        ok(call("POST", path + "rebuild", Map.of(), "Article", "root", Map.of()));
        assertEquals("rebuild 0", Models.CALLS.get(1));
        assertEquals(404, call("POST", path + "missing", Map.of()).status());
        Answer form = call("POST", path + "publish/form-value", null);
        assertEquals(200, form.status());
        assertTrue(form.map().containsKey("note"));
        assertNull(form.map().get("note"));
        assertEquals(404, call("POST", path + "feature/form-value", null).status());
    }

    @Test
    void illegalInput() {
        fail(call("POST", "/erupt-api/data/modify/Article", Map.of("status", "draft")));
        fail(call("POST", "/erupt-api/data/modify/Article", Map.of("title", "x", "status", "deleted")));
        fail(call("POST", "/erupt-api/data/modify/Article", Map.of("title", "x", "level", "MEDIUM")));
        fail(call("POST", "/erupt-api/data/modify/Article", Map.of("title", "x", "views", "many")));
        fail(call("POST", "/erupt-api/data/modify/Article", Map.of("title", "x", "views", 1.5)));
        fail(call("POST", "/erupt-api/data/modify/Article", Map.of("title", "x", "day", "someday")));
        fail(call("POST", "/erupt-api/data/table/Article", Map.of("condition", List.of(cond("views", "EQ", 1)))));
        fail(call("POST", "/erupt-api/data/table/Article", Map.of("condition", List.of(cond("title\" OR 1=1 --", "EQ", 1)))));
        fail(call("POST", "/erupt-api/data/table/Article", Map.of("sort", List.of(Map.of("field", "stamp", "direction", "ASC")))));
        fail(call("POST", "/erupt-api/data/table/Article", Map.of("condition", List.of(cond("title", "DROP", 1)))));
        fail(call("POST", "/erupt-api/data/modify/Article", "{not json".getBytes(StandardCharsets.UTF_8)));
        fail(call("POST", "/erupt-api/data/modify/Article/delete", Map.of("id", 1)));
        fail(call("GET", "/erupt-api/data/Article/abc", null));
    }

    @Test
    void declaration() {
        assertThrows(IllegalArgumentException.class, () -> Model.of(String.class), "not an @Erupt class");
        assertThrows(IllegalArgumentException.class, () -> node.register(Article.class, new MemoryRepository<>()), "registered twice");
        assertEquals(List.of("id", "text"), Model.of(Note.class).columns());
    }
}
