package xyz.erupt.file.codec;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.annotation.FileType;

import java.util.*;

/**
 * YAML codec, registered only when SnakeYAML is on the classpath. A top-level
 * sequence is a list of records; a top-level mapping is a single record. Written
 * back in block style — as a mapping when {@code single = true}, otherwise a
 * sequence. Reading tolerates either shape.
 *
 * @author YuePeng
 */
public class YamlCodec implements FileCodec {

    private static final Set<String> EXTENSIONS = Set.of("yml", "yaml");

    @Override
    public FileType type() {
        return FileType.YAML;
    }

    @Override
    public boolean accept(String path) {
        return EXTENSIONS.contains(FileCodec.extension(path));
    }

    @Override
    public boolean singleton(EruptFile eruptFile) {
        return eruptFile.single();
    }

    @Override
    public List<JsonObject> decode(String content, EruptFile eruptFile) {
        List<JsonObject> list = new ArrayList<>();
        Object loaded = new Yaml().load(content);
        if (null == loaded) return list;
        Gson gson = GsonFactory.getGson();
        if (loaded instanceof List) {
            for (Object item : (List<?>) loaded) {
                if (item instanceof Map) list.add(gson.toJsonTree(item).getAsJsonObject());
            }
        } else if (loaded instanceof Map) {
            list.add(gson.toJsonTree(loaded).getAsJsonObject());
        }
        return list;
    }

    @Override
    public String encode(EruptModel eruptModel, EruptFile eruptFile, List<JsonObject> records) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        if (eruptFile.single()) {
            Object data = records.isEmpty() ? new LinkedHashMap<>() : FileCodec.jsonToJava(records.get(0));
            return yaml.dump(data);
        }
        List<Object> data = new ArrayList<>();
        for (JsonObject record : records) data.add(FileCodec.jsonToJava(record));
        return yaml.dump(data);
    }

}
