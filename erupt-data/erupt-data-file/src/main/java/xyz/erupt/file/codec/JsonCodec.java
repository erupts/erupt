package xyz.erupt.file.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.annotation.FileType;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON codec and the fallback for any unclaimed extension (so {@code .json},
 * {@code .txt} or an extension-less path all round-trip as JSON). A top-level
 * array is a list of records; a top-level object is a single record. When
 * {@code single = true} the file is written back as one object, otherwise as an
 * array. Reading is tolerant of either shape regardless of the flag.
 *
 * @author YuePeng
 */
public class JsonCodec implements FileCodec {

    @Override
    public FileType type() {
        return FileType.JSON;
    }

    // fallback codec: claims every path, so it must be registered last
    @Override
    public boolean accept(String path) {
        return true;
    }

    @Override
    public boolean singleton(EruptFile eruptFile) {
        return eruptFile.single();
    }

    @Override
    public List<JsonObject> decode(String content, EruptFile eruptFile) {
        List<JsonObject> list = new ArrayList<>();
        JsonElement root = GsonFactory.getGson().fromJson(content, JsonElement.class);
        if (null == root || root.isJsonNull()) return list;
        if (root.isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray()) {
                if (element.isJsonObject()) list.add(element.getAsJsonObject());
            }
        } else if (root.isJsonObject()) {
            list.add(root.getAsJsonObject());
        }
        return list;
    }

    @Override
    public String encode(EruptModel eruptModel, EruptFile eruptFile, List<JsonObject> records) {
        if (eruptFile.single()) {
            return GsonFactory.getGson().toJson(records.isEmpty() ? new JsonObject() : records.get(0));
        }
        JsonArray array = new JsonArray();
        records.forEach(array::add);
        return GsonFactory.getGson().toJson(array);
    }

}
