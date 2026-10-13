package xyz.erupt.test.cloud;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.test.EruptApplicationTests;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Keeps schema/erupt-node-defaults.json in step with the annotations: the file is what the Java proxies
 * emit for each supported edit type, so a node SDK in another language only overrides values on top of
 * it instead of re-implementing annotation defaults. Fails when the committed file drifts;
 * regenerate with {@code mvn test -pl erupt-test -Dtest=NodeDefaultsTest -Derupt.node-defaults.write=true}.
 */
public class NodeDefaultsTest extends EruptApplicationTests {

    private static final Path FILE = Path.of("..", "schema", "erupt-node-defaults.json");

    @Test
    void nodeDefaults() throws Exception {
        EruptModel model = EruptCoreService.getEruptView(NodeDefaultsFixture.class.getSimpleName());
        JsonObject root = new JsonObject();
        JsonObject erupt = model.getEruptJson().deepCopy();
        root.add("rowOperation", erupt.remove("rowOperation").getAsJsonArray().get(0));
        erupt.add("rowOperation", new JsonArray());
        root.add("erupt", erupt);
        root.add("power", GsonFactory.getGson().toJsonTree(new PowerObject()));
        JsonObject fields = new JsonObject();
        for (EruptFieldModel field : model.getEruptFieldModels()) {
            JsonObject json = GsonFactory.getGson().toJsonTree(field).getAsJsonObject();
            json.remove("fieldName");
            json.remove("componentValue");
            fields.add(field.getFieldName(), json);
        }
        root.add("fields", fields);
        JsonElement generated = sorted(root);
        String text = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(generated) + "\n";
        if (Boolean.getBoolean("erupt.node-defaults.write")) {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, text, StandardCharsets.UTF_8);
        } else {
            assertEquals(text, Files.readString(FILE, StandardCharsets.UTF_8),
                    FILE + " is stale, regenerate with -Derupt.node-defaults.write=true");
        }
    }

    // Reflection order of annotation methods is unspecified; sort keys so the committed file diffs cleanly
    private static JsonElement sorted(JsonElement element) {
        if (element.isJsonObject()) {
            Map<String, JsonElement> map = new TreeMap<>();
            element.getAsJsonObject().entrySet().forEach(e -> map.put(e.getKey(), sorted(e.getValue())));
            JsonObject result = new JsonObject();
            map.forEach(result::add);
            return result;
        } else if (element.isJsonArray()) {
            JsonArray result = new JsonArray();
            element.getAsJsonArray().forEach(e -> result.add(sorted(e)));
            return result;
        }
        return element;
    }

}
