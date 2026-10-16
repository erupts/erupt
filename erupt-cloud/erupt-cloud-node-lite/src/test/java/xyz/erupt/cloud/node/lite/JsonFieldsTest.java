package xyz.erupt.cloud.node.lite;

import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.cloud.common.http.CloudSignature;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * List / Set / Map members (MULTI_CHOICE, KEY_VALUE) stored as JSON text, and layout controls and transient
 * members kept out of the repository, through the node's endpoints on the memory and the JDBC repository.
 */
class JsonFieldsTest {

    static final String TOKEN = "node-secret";

    @Erupt(name = "Profile")
    static class Profile {
        @EruptField
        Long id;
        @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", notNull = true))
        String name;
        // AUTO: a collection is a MULTI_CHOICE, an enum element offers its constants
        @EruptField(views = @View(title = "Roles"), edit = @Edit(title = "Roles", search = @Search))
        List<Role> roles;
        @EruptField(views = @View(title = "Languages"), edit = @Edit(title = "Languages", type = EditType.MULTI_CHOICE, search = @Search,
                multiChoiceType = @MultiChoiceType(vl = {@VL(value = "JAVA", label = "Java"), @VL(value = "GO", label = "Go")})))
        List<String> langs;
        @EruptField(views = @View(title = "Attributes"), edit = @Edit(title = "Attributes", type = EditType.KEY_VALUE))
        Map<String, String> attrs;
        @EruptField(edit = @Edit(title = "Basic", type = EditType.DIVIDE))
        String basic;
        @EruptField(views = @View(title = "Score"), edit = @Edit(title = "Score"))
        transient Integer score;
    }

    enum Role {ADMIN, USER, GUEST}

    @Test
    void memory() {
        run(MemoryRepository::new);
    }

    @Test
    void jdbc() {
        run(() -> {
            DataSource ds = JdbcRepositoryTest.h2();
            try {
                // no column for the divide or the transient score: neither is stored
                JdbcRepositoryTest.execute(ds, "CREATE TABLE \"profile\" (\"id\" BIGINT AUTO_INCREMENT PRIMARY KEY, \"name\" VARCHAR(64), "
                        + "\"roles\" VARCHAR(255), \"langs\" VARCHAR(255), \"attrs\" VARCHAR(1024))");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return new JdbcRepository<>(ds, "profile");
        });
    }

    @SuppressWarnings("unchecked")
    private void run(Supplier<Repository<Profile>> repository) {
        Node node = new Node("blog", TOKEN, "http://127.0.0.1:1").register(Profile.class, repository.get());
        assertEquals(List.of("id", "name", "roles", "langs", "attrs"), node.model(Profile.class).columns());

        Map<String, Object> build = (Map<String, Object>) call(node, "GET", "/erupt-api/build/Profile", null);
        Map<String, Map<String, Object>> fields = new HashMap<>();
        for (Object f : (List<?>) ((Map<String, Object>) build.get("eruptModel")).get("eruptFieldModels")) {
            fields.put((String) ((Map<String, Object>) f).get("fieldName"), (Map<String, Object>) f);
        }
        assertEquals("MULTI_CHOICE", ((Map<String, Object>) ((Map<String, Object>) fields.get("roles").get("eruptFieldJson")).get("edit")).get("type"));
        assertEquals(List.of("ADMIN", "USER", "GUEST"), values(fields.get("roles")));
        assertEquals(List.of("JAVA", "GO"), values(fields.get("langs")));

        Map<String, Object> added = (Map<String, Object>) call(node, "POST", "/erupt-api/data/modify/Profile", Map.of("name", "A",
                "roles", List.of("ADMIN", "USER"), "langs", List.of("JAVA"), "attrs", Map.of("env", "prod"), "basic", "x", "score", 3));
        assertEquals(true, added.get("success"), String.valueOf(added));
        Map<String, Object> row = rows(node, List.of()).get(0);
        assertEquals(List.of("ADMIN", "USER"), row.get("roles"));
        assertEquals(List.of("JAVA"), row.get("langs"));
        assertEquals(Map.of("env", "prod"), row.get("attrs"));

        // a MULTI_CHOICE matches by one element
        assertEquals(1, rows(node, List.of(Map.of("key", "roles", "value", "ADMIN"))).size());
        assertEquals(0, rows(node, List.of(Map.of("key", "roles", "value", "GUEST"))).size());
        assertEquals(0, rows(node, List.of(Map.of("key", "langs", "expression", "IN", "value", List.of("GO")))).size());

        Map<String, Object> illegal = (Map<String, Object>) call(node, "POST", "/erupt-api/data/modify/Profile",
                Map.of("name", "B", "langs", List.of("RUST")));
        assertEquals(false, illegal.get("success"), "an option the field does not offer");

        Map<String, Object> update = new HashMap<>(row);
        update.put("langs", List.of("GO"));
        update.put("attrs", Map.of());
        assertEquals(true, ((Map<String, Object>) call(node, "POST", "/erupt-api/data/modify/Profile/update", update)).get("success"));
        Map<String, Object> read = (Map<String, Object>) call(node, "GET", "/erupt-api/data/Profile/" + row.get("id"), null);
        assertEquals(List.of("GO"), read.get("langs"));
        assertEquals(Map.of(), read.get("attrs"));
    }

    @SuppressWarnings("unchecked")
    private static List<Object> values(Map<String, Object> field) {
        return ((List<Map<String, Object>>) field.get("componentValue")).stream().map(o -> o.get("value")).toList();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> rows(Node node, List<Map<String, Object>> condition) {
        Map<String, Object> page = (Map<String, Object>) call(node, "POST", "/erupt-api/data/table/Profile",
                Map.of("pageIndex", 1, "pageSize", 10, "condition", condition));
        return (List<Map<String, Object>>) page.get("list");
    }

    // a signed call with power vouched for, so the node does not ask the (absent) server
    private static Object call(Node node, String method, String target, Object body) {
        byte[] raw = body == null ? new byte[0] : Json.write(body).getBytes(StandardCharsets.UTF_8);
        String user = Base64.getEncoder().encodeToString("{\"uid\":1,\"account\":\"admin\"}".getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis() / 1000;
        // looked up lower-cased, as the node asks for them case-insensitively
        Map<String, String> headers = Map.of("erupt", "Profile", "user", user, "erupt-timestamp", String.valueOf(now), "erupt-enforced", "power",
                "erupt-signature", CloudSignature.sign(TOKEN, method, target, now, "power", user, raw));
        Node.Response r = node.handle(new Node.Request(method, target, k -> headers.get(k.toLowerCase()), raw));
        assertEquals(200, r.status(), new String(r.body(), StandardCharsets.UTF_8));
        return Json.parse(new String(r.body(), StandardCharsets.UTF_8));
    }
}
