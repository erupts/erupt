package xyz.erupt.core.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.view.EruptModel;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared plumbing for SaaS "table" data sources (Feishu Bitable, DingTalk
 * Notable, Airtable, ...): a model is bound to one remote table by a type
 * annotation {@code A}, every record is a {@code fields} JSON object plus an
 * id, and the whole table is fetched over REST and then filtered / sorted /
 * paged in memory by {@link EruptBeanDataService}.
 * <p>
 * This class owns the HTTP transport, the JSON-to-Java flattening of cell
 * values and the model-to-{@code fields} encoding, so a concrete connector only
 * needs to know its endpoints, its auth header and its envelope.
 *
 * @author YuePeng
 */
public abstract class EruptRestTableDataService<A extends Annotation> extends EruptBeanDataService<Map<String, Object>> {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL).build();

    // guard so a huge remote table cannot exhaust memory in LOCAL query mode
    protected static final int MAX_FETCH = 5000;

    private final Class<A> bindingClass;

    protected EruptRestTableDataService(Class<A> bindingClass) {
        this.bindingClass = bindingClass;
    }

    /**
     * Translated message prefixed to every transport / API error.
     */
    protected abstract String requestFailedMessage();

    /**
     * The table binding annotation on the model; missing it is a programming error.
     */
    protected A binding(EruptModel eruptModel) {
        A binding = eruptModel.getClazz().getAnnotation(this.bindingClass);
        if (null == binding) {
            throw new EruptWebApiRuntimeException("@" + this.bindingClass.getSimpleName()
                    + " annotation is missing on " + eruptModel.getEruptName());
        }
        return binding;
    }

    protected Object id(EruptModel eruptModel, Object object) {
        return this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
    }

    // --- field mapping ---------------------------------------------------------------------

    /**
     * Decode a record's {@code fields} object into a row and stamp the remote id on
     * the model primary key.
     */
    protected Map<String, Object> row(EruptModel eruptModel, JsonObject fields, String id) {
        Map<String, Object> row = new LinkedHashMap<>();
        if (null != fields) {
            for (Map.Entry<String, JsonElement> entry : fields.entrySet()) {
                row.put(entry.getKey(), fieldValue(entry.getValue()));
            }
        }
        row.put(eruptModel.getErupt().primaryKeyCol(), id);
        return row;
    }

    /**
     * Flatten a cell value to plain Java: primitives as clean scalars, an array of
     * primitives (multi-select, linked ids) as a list, and arrays / objects carrying
     * {@code text} / {@code name} / {@code link} / {@code url} (rich text, person,
     * attachment, hyperlink) as a joined string.
     */
    public static Object fieldValue(JsonElement element) {
        if (null == element || element.isJsonNull()) return null;
        if (element.isJsonPrimitive()) return primitive(element.getAsJsonPrimitive());
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (!array.isEmpty() && array.get(0).isJsonObject()) {
                StringBuilder sb = new StringBuilder();
                for (JsonElement segment : array) sb.append(segmentText(segment));
                return sb.toString();
            }
            List<Object> list = new ArrayList<>();
            for (JsonElement item : array) list.add(fieldValue(item));
            return list;
        }
        return segmentText(element);
    }

    private static String segmentText(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (String key : new String[]{"text", "name", "en_name", "link", "url", "email"}) {
                if (object.has(key) && object.get(key).isJsonPrimitive()) return object.get(key).getAsString();
            }
        } else if (element.isJsonPrimitive()) {
            return String.valueOf(primitive(element.getAsJsonPrimitive()));
        }
        return element.toString();
    }

    private static Object primitive(JsonPrimitive primitive) {
        if (primitive.isBoolean()) return primitive.getAsBoolean();
        if (primitive.isNumber()) {
            BigDecimal number = primitive.getAsBigDecimal();
            return number.stripTrailingZeros().scale() <= 0 ? (Object) number.longValue() : (Object) number.doubleValue();
        }
        return primitive.getAsString();
    }

    /**
     * Model → {@code fields}: scalar / array values travel as-is; the primary key
     * (owned by the remote side) and nulls are dropped.
     */
    protected JsonObject encodeFields(EruptModel eruptModel, Object object) {
        String primaryKey = eruptModel.getErupt().primaryKeyCol();
        JsonObject source = GsonFactory.getGson().toJsonTree(object).getAsJsonObject();
        JsonObject fields = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            if (entry.getKey().equals(primaryKey) || entry.getValue().isJsonNull()) continue;
            fields.add(entry.getKey(), entry.getValue());
        }
        return fields;
    }

    // --- transport -------------------------------------------------------------------------

    /**
     * Send a JSON request and return the parsed body. Non-2xx responses raise with the
     * status and the body, since every one of these platforms puts the real cause
     * (invalid token / unknown field / page size) in a JSON error body.
     *
     * @param headers alternating name / value pairs
     */
    protected JsonObject send(String method, String url, String body, String... headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(15));
        for (int i = 0; i + 1 < headers.length; i += 2) builder.header(headers[i], headers[i + 1]);
        if (null != body) builder.header("Content-Type", "application/json; charset=utf-8");
        builder.method(method, null == body ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new EruptWebApiRuntimeException(this.requestFailedMessage()
                        + " → " + response.statusCode() + " " + method + " " + url
                        + (isBlank(response.body()) ? "" : " " + response.body()));
            }
            JsonElement json = isBlank(response.body()) ? null
                    : GsonFactory.getGson().fromJson(response.body(), JsonElement.class);
            return null != json && json.isJsonObject() ? json.getAsJsonObject() : new JsonObject();
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException(this.requestFailedMessage() + " → " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EruptWebApiRuntimeException(this.requestFailedMessage() + " → " + e.getMessage());
        }
    }

    protected static JsonArray array(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonArray() ? object.getAsJsonArray(key) : new JsonArray();
    }

    protected static String string(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : null;
    }

    protected static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    protected static boolean isBlank(String value) {
        return null == value || value.trim().isEmpty();
    }

}
