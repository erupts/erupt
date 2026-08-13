package xyz.erupt.file.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.annotation.FileType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A file format handler: turns a file's text into a list of JSON records and back.
 * Each record maps one-to-one to a model bean via Gson, so codecs stay ignorant of
 * the entity type and only deal with the encoding surface (CSV rows, JSON tree,
 * YAML nodes, properties keys, markdown front-matter ...).
 *
 * @author YuePeng
 */
public interface FileCodec {

    /**
     * The format this codec implements, matched against an explicit
     * {@link EruptFile#type()}.
     */
    FileType type();

    /**
     * Whether this codec handles the given file path, decided by its extension.
     * Used only when the model leaves {@link EruptFile#type()} at {@link FileType#AUTO}.
     */
    boolean accept(String path);

    /**
     * Whether the format stores exactly one record per file (a pure settings form)
     * rather than a list. List formats return {@code false}.
     */
    default boolean singleton(EruptFile eruptFile) {
        return false;
    }

    /**
     * Parse non-blank file content into one JSON object per record.
     */
    List<JsonObject> decode(String content);

    /**
     * Serialize records back to file text; the model supplies field order for
     * column-oriented formats.
     */
    String encode(EruptModel eruptModel, EruptFile eruptFile, List<JsonObject> records);

    /**
     * Lower-case extension without the dot, or empty when the path has none.
     */
    static String extension(String path) {
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        int dot = path.lastIndexOf('.');
        return dot > slash ? path.substring(dot + 1).toLowerCase() : "";
    }

    /**
     * Convert a JSON element to plain Java (Long / Double / Boolean / String / Map / List)
     * so text formats emit clean scalars — integral numbers as {@code 1}, not {@code 1.0}.
     */
    static Object jsonToJava(JsonElement element) {
        if (null == element || element.isJsonNull()) return null;
        if (element.isJsonObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            element.getAsJsonObject().entrySet().forEach(e -> map.put(e.getKey(), jsonToJava(e.getValue())));
            return map;
        }
        if (element.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            element.getAsJsonArray().forEach(e -> list.add(jsonToJava(e)));
            return list;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) return primitive.getAsBoolean();
        if (primitive.isNumber()) {
            BigDecimal number = primitive.getAsBigDecimal();
            return number.stripTrailingZeros().scale() <= 0 ? (Object) number.longValue() : (Object) number.doubleValue();
        }
        return primitive.getAsString();
    }

    /**
     * Render a JSON value as its scalar text, falling back to compact JSON for
     * nested objects / arrays.
     */
    static String asText(JsonElement element) {
        return element.isJsonPrimitive() ? element.getAsString() : element.toString();
    }

}
