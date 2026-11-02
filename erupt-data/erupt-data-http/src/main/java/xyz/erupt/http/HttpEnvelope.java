package xyz.erupt.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Reads records out of JSON response envelopes by dotted path ({@code data.items},
 * {@code meta.count}). An empty path means "no envelope": a list is a plain array or a
 * {@code list} property, a total is {@code total}, an item is the whole body.
 *
 * @author YuePeng
 */
public final class HttpEnvelope {

    private static final Type LIST_TYPE = new TypeToken<List<Map<String, Object>>>() {
    }.getType();

    private static final String LIST = "list";

    private static final String TOTAL = "total";

    private static final String[] MESSAGE_KEYS = {"message", "msg", "error", "error_description"};

    private HttpEnvelope() {
    }

    public static List<Map<String, Object>> list(String body, String listPath) {
        JsonElement root = parse(body);
        JsonElement list = listPath.isEmpty()
                ? root.isJsonArray() ? root : member(root, LIST)
                : path(root, listPath);
        if (null == list || !list.isJsonArray()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("http.unexpected_response"));
        }
        return GsonFactory.getGson().fromJson(list, LIST_TYPE);
    }

    public static Long total(String body, String totalPath) {
        JsonElement root = parse(body);
        JsonElement total = totalPath.isEmpty() ? member(root, TOTAL) : path(root, totalPath);
        return null == total || !total.isJsonPrimitive() ? null : total.getAsLong();
    }

    public static <T> T item(String body, String itemPath, Class<T> clazz) {
        JsonElement root = parse(body);
        JsonElement item = itemPath.isEmpty() ? root : path(root, itemPath);
        if (null == item) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("http.unexpected_response"));
        return GsonFactory.getGson().fromJson(item, clazz);
    }

    /**
     * A human-readable error string from an error body, or {@code null} if there is none.
     */
    public static String message(String body) {
        if (null == body || body.isBlank()) return null;
        try {
            JsonElement root = GsonFactory.getGson().fromJson(body, JsonElement.class);
            if (!root.isJsonObject()) return null;
            for (String key : MESSAGE_KEYS) {
                JsonElement value = root.getAsJsonObject().get(key);
                if (null != value && value.isJsonPrimitive()) return value.getAsString();
            }
        } catch (JsonSyntaxException ignore) {
            // not JSON: nothing worth surfacing
        }
        return null;
    }

    /**
     * Walk a dotted path; {@code null} when any segment is missing.
     */
    static JsonElement path(JsonElement root, String path) {
        JsonElement current = root;
        for (String segment : path.split("\\.")) {
            current = member(current, segment);
            if (null == current) return null;
        }
        return current;
    }

    private static JsonElement member(JsonElement element, String name) {
        if (null == element || !element.isJsonObject()) return null;
        JsonObject object = element.getAsJsonObject();
        return object.has(name) && !object.get(name).isJsonNull() ? object.get(name) : null;
    }

    private static JsonElement parse(String body) {
        try {
            JsonElement root = GsonFactory.getGson().fromJson(body, JsonElement.class);
            if (null == root) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("http.unexpected_response"));
            return root;
        } catch (JsonSyntaxException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("http.unexpected_response") + " → " + e.getMessage());
        }
    }

}
