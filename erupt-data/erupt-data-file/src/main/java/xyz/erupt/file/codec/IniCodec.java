package xyz.erupt.file.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.annotation.FileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * INI mapped to a single record — a superset of {@code .properties} that adds
 * {@code [section]} grouping. Keys before any section become top-level fields;
 * each {@code [section]} becomes a nested object field of the same name, so the
 * model declares a bean-typed field per section. Lines starting with {@code ;} or
 * {@code #} are comments; keys accept {@code =} or {@code :} as the separator.
 * Written back with all scalar fields first, then one section per object field
 * (in declared order) to keep the output valid.
 *
 * @author YuePeng
 */
public class IniCodec implements FileCodec {

    @Override
    public FileType type() {
        return FileType.INI;
    }

    @Override
    public boolean accept(String path) {
        return "ini".equals(FileCodec.extension(path));
    }

    @Override
    public boolean singleton(EruptFile eruptFile) {
        return true;
    }

    @Override
    public List<JsonObject> decode(String content, EruptFile eruptFile) {
        JsonObject root = new JsonObject();
        JsonObject current = root;
        for (String raw : content.split("\n")) {
            String line = raw.trim();
            if (line.isEmpty() || line.charAt(0) == ';' || line.charAt(0) == '#') continue;
            if (line.charAt(0) == '[' && line.endsWith("]")) {
                current = new JsonObject();
                root.add(line.substring(1, line.length() - 1).trim(), current);
                continue;
            }
            int sep = this.separator(line);
            if (sep < 0) continue;
            current.addProperty(line.substring(0, sep).trim(), line.substring(sep + 1).trim());
        }
        List<JsonObject> list = new ArrayList<>();
        list.add(root);
        return list;
    }

    @Override
    public String encode(EruptModel eruptModel, EruptFile eruptFile, List<JsonObject> records) {
        if (records.isEmpty()) return "";
        JsonObject json = records.get(0);
        StringBuilder sb = new StringBuilder();
        // scalar fields first, so every key belongs to the implicit global section
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            JsonElement value = json.get(fieldModel.getFieldName());
            if (null == value || value.isJsonNull() || value.isJsonObject()) continue;
            sb.append(fieldModel.getFieldName()).append('=').append(FileCodec.asText(value)).append('\n');
        }
        // object fields become sections
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            JsonElement value = json.get(fieldModel.getFieldName());
            if (null == value || !value.isJsonObject()) continue;
            sb.append('[').append(fieldModel.getFieldName()).append("]\n");
            for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
                if (null == entry.getValue() || entry.getValue().isJsonNull()) continue;
                sb.append(entry.getKey()).append('=').append(FileCodec.asText(entry.getValue())).append('\n');
            }
        }
        return sb.toString();
    }

    private int separator(String line) {
        int eq = line.indexOf('=');
        int colon = line.indexOf(':');
        if (eq < 0) return colon;
        if (colon < 0) return eq;
        return Math.min(eq, colon);
    }

}
