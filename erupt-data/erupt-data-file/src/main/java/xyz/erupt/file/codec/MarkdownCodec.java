package xyz.erupt.file.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.annotation.FileType;

import java.util.ArrayList;
import java.util.List;

/**
 * Markdown with YAML-style front-matter mapped to a single record: the
 * {@code key: value} lines between the leading {@code ---} fences become fields,
 * and the body after the closing fence maps to the {@code content} field (declare
 * one, e.g. an {@code HTML_EDITOR}, to edit the body). A file without front-matter
 * is treated as pure body.
 *
 * @author YuePeng
 */
public class MarkdownCodec implements FileCodec {

    // conventional field the document body is bound to
    public static final String BODY_FIELD = "content";

    private static final String FENCE = "---";

    @Override
    public FileType type() {
        return FileType.MARKDOWN;
    }

    @Override
    public boolean accept(String path) {
        String extension = FileCodec.extension(path);
        return "md".equals(extension) || "markdown".equals(extension);
    }

    @Override
    public boolean singleton(EruptFile eruptFile) {
        return true;
    }

    @Override
    public List<JsonObject> decode(String content, EruptFile eruptFile) {
        JsonObject json = new JsonObject();
        String body = content;
        String[] lines = content.split("\n", -1);
        if (lines.length > 0 && lines[0].trim().equals(FENCE)) {
            int end = -1;
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].trim().equals(FENCE)) {
                    end = i;
                    break;
                }
            }
            if (end > 0) {
                for (int i = 1; i < end; i++) {
                    int colon = lines[i].indexOf(':');
                    if (colon < 0) continue;
                    String key = lines[i].substring(0, colon).trim();
                    if (!key.isEmpty()) json.addProperty(key, this.stripQuotes(lines[i].substring(colon + 1).trim()));
                }
                StringBuilder rest = new StringBuilder();
                for (int i = end + 1; i < lines.length; i++) {
                    if (rest.length() > 0 || i > end + 1) rest.append('\n');
                    rest.append(lines[i]);
                }
                body = rest.toString().stripLeading();
            }
        }
        if (!body.isEmpty()) json.addProperty(BODY_FIELD, body);
        List<JsonObject> list = new ArrayList<>();
        list.add(json);
        return list;
    }

    @Override
    public String encode(EruptModel eruptModel, EruptFile eruptFile, List<JsonObject> records) {
        if (records.isEmpty()) return "";
        JsonObject json = records.get(0);
        StringBuilder sb = new StringBuilder(FENCE).append('\n');
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            String key = fieldModel.getFieldName();
            if (BODY_FIELD.equals(key)) continue;
            JsonElement value = json.get(key);
            if (null == value || value.isJsonNull()) continue;
            sb.append(key).append(": ").append(this.quoteIfNeeded(FileCodec.asText(value))).append('\n');
        }
        sb.append(FENCE).append('\n');
        JsonElement body = json.get(BODY_FIELD);
        if (null != body && !body.isJsonNull()) sb.append(body.getAsString());
        return sb.toString();
    }

    private String stripQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).replace("\\\"", "\"");
        }
        return value;
    }

    private String quoteIfNeeded(String value) {
        if (value.isEmpty() || value.contains(":") || value.contains("#")
                || !value.equals(value.trim())) {
            return '"' + value.replace("\"", "\\\"") + '"';
        }
        return value;
    }

}
