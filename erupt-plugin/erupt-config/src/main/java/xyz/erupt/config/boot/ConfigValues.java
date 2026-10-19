package xyz.erupt.config.boot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * The item types and how a stored value becomes properties. Every type is one property, {@code key},
 * except the structured ones: KEY_VALUE, JSON and YAML are flattened into {@code key.path} and
 * {@code key[i]} properties so a Map, a List or a {@code @ConfigurationProperties} bean binds to
 * them (JSON and YAML keep the raw text under {@code key} as well). Shared by the startup reader
 * and the runtime refresh, so both see the same properties.
 * <p>
 * Types are string constants on purpose: an enum column would freeze its value list into a check
 * constraint on databases already in the field, and this list grows.
 *
 * @author YuePeng
 */
public final class ConfigValues {

    public static final String STRING = "STRING";

    /** A string typed behind dots; always a secret. */
    public static final String PASSWORD = "PASSWORD";

    public static final String TEXT = "TEXT";

    public static final String NUMBER = "NUMBER";

    public static final String BOOLEAN = "BOOLEAN";

    /** Tags stored comma-separated, which Spring binds to a {@code List} or an array. */
    public static final String LIST = "LIST";

    public static final String KEY_VALUE = "KEY_VALUE";

    public static final String JSON = "JSON";

    public static final String YAML = "YAML";

    /** ISO date, {@code 2026-10-20}; Spring binds it to a {@code LocalDate}. */
    public static final String DATE = "DATE";

    /** ISO date-time, {@code 2026-10-20T09:30:00}; Spring binds it to a {@code LocalDateTime}. */
    public static final String DATETIME = "DATETIME";

    /** ISO time, {@code 09:30:00}; Spring binds it to a {@code LocalTime}. */
    public static final String TIME = "TIME";

    public static final String COLOR = "COLOR";

    /** An icon's class list, {@code fa fa-bell}, as erupt's icon picker stores it. */
    public static final String ICON = "ICON";

    public static final String HTML = "HTML";

    /** An uploaded picture; the value is its path under the upload directory, as erupt stores attachments. */
    public static final String IMAGE = "IMAGE";

    /** An uploaded file; the value is its path under the upload directory, as erupt stores attachments. */
    public static final String ATTACHMENT = "ATTACHMENT";

    private ConfigValues() {
    }

    public static void flatten(String key, String type, String value, Map<String, Object> into) {
        String text = null == value ? "" : value;
        switch (null == type ? STRING : type) {
            case KEY_VALUE -> {
                JsonElement element = parseJson(text);
                if (null != element && element.isJsonObject()) {
                    flattenJson(key, element, into);
                    return;
                }
            }
            case JSON -> {
                JsonElement element = parseJson(text);
                if (null != element && (element.isJsonObject() || element.isJsonArray())) {
                    into.put(key, text);
                    flattenJson(key, element, into);
                    return;
                }
            }
            case YAML -> {
                Properties properties = parseYaml(text);
                if (null != properties) {
                    into.put(key, text);
                    properties.forEach((name, item) -> into.put(key + "." + name, String.valueOf(item)));
                    return;
                }
            }
            default -> {
            }
        }
        into.put(key, text);
    }

    private static void flattenJson(String prefix, JsonElement element, Map<String, Object> into) {
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) flattenJson(prefix + "." + entry.getKey(), entry.getValue(), into);
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) flattenJson(prefix + "[" + i + "]", array.get(i), into);
        } else {
            into.put(prefix, element.isJsonNull() ? "" : element.getAsString());
        }
    }

    /** @return the parsed element, or null when the text is not JSON; blank text is an empty object */
    public static JsonElement parseJson(String text) {
        if (null == text || text.isBlank()) return new JsonObject();
        try {
            return JsonParser.parseString(text);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** @return the flattened properties, or null when the text is not YAML; blank text is empty */
    public static Properties parseYaml(String text) {
        if (null == text || text.isBlank()) return new Properties();
        try {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(new ByteArrayResource(text.getBytes(StandardCharsets.UTF_8)));
            return factory.getObject();
        } catch (RuntimeException e) {
            return null;
        }
    }

}
