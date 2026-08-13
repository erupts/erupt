package xyz.erupt.file.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.annotation.FileType;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON Lines ({@code .jsonl} / {@code .ndjson}): one JSON object per line, so a
 * record maps to a single line and the file is inherently a list — the natural
 * shape for append-friendly exports and logs. Blank lines are skipped; each
 * non-blank line must be a JSON object. Nested fields are supported, like
 * {@link JsonCodec}, but records never span lines.
 *
 * @author YuePeng
 */
public class JsonlCodec implements FileCodec {

    @Override
    public FileType type() {
        return FileType.JSONL;
    }

    @Override
    public boolean accept(String path) {
        String ext = FileCodec.extension(path);
        return "jsonl".equals(ext) || "ndjson".equals(ext);
    }

    @Override
    public List<JsonObject> decode(String content, EruptFile eruptFile) {
        List<JsonObject> list = new ArrayList<>();
        for (String line : content.split("\n")) {
            if (line.trim().isEmpty()) continue;
            JsonElement element = GsonFactory.getGson().fromJson(line, JsonElement.class);
            if (null != element && element.isJsonObject()) list.add(element.getAsJsonObject());
        }
        return list;
    }

    @Override
    public String encode(EruptModel eruptModel, EruptFile eruptFile, List<JsonObject> records) {
        StringBuilder sb = new StringBuilder();
        for (JsonObject json : records) sb.append(GsonFactory.getGson().toJson(json)).append('\n');
        return sb.toString();
    }

}
