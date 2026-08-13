package xyz.erupt.notion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.notion.annotation.EruptNotion;
import xyz.erupt.notion.prop.EruptNotionProperties;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Notion database data source: models annotated with {@link EruptNotion} are read
 * from and written to a Notion database over the REST API. Query mode is LOCAL —
 * the whole database is fetched (cursor-paged) and the base class filters / sorts /
 * pages it in memory, so this class only supplies {@link #data} plus CRUD. The
 * integration token comes from {@code erupt.notion.*}. Each page's typed properties
 * are flattened to plain scalars on read; on write they are re-wrapped by type,
 * driven by the database schema (fetched once and cached). Delete is a soft delete:
 * Notion has no hard delete, so the page is archived (and thereby leaves the list).
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class EruptNotionDataService extends EruptBeanDataService<Map<String, Object>> {

    public static final String DATA_PROCESSOR = "NOTION";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptNotionDataService.class);
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL).build();

    // Notion query page_size ceiling; and a guard so a huge database cannot exhaust memory
    private static final int PAGE_SIZE = 100;

    private static final int MAX_FETCH = 5000;

    @Resource
    private EruptNotionProperties properties;

    // databaseId -> (property name -> property type), so writes can re-wrap values
    private final Map<String, Map<String, String>> schemaCache = new ConcurrentHashMap<>();

    @Override
    protected List<Map<String, Object>> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        EruptNotion notion = this.eruptNotion(eruptModel);
        String primaryKey = eruptModel.getErupt().primaryKeyCol();
        List<Map<String, Object>> rows = new ArrayList<>();
        String cursor = null;
        do {
            JsonObject payload = new JsonObject();
            payload.addProperty("page_size", PAGE_SIZE);
            if (null != cursor) payload.addProperty("start_cursor", cursor);
            String url = this.properties.getBaseUrl() + "/v1/databases/" + notion.databaseId() + "/query";
            JsonObject json = GsonFactory.getGson().fromJson(this.request("POST", url, payload.toString()), JsonObject.class);
            JsonArray results = json.has("results") && json.get("results").isJsonArray()
                    ? json.getAsJsonArray("results") : new JsonArray();
            for (JsonElement element : results) {
                JsonObject page = element.getAsJsonObject();
                Map<String, Object> row = this.decodeProperties(page.getAsJsonObject("properties"));
                row.put(primaryKey, page.get("id").getAsString());
                rows.add(row);
            }
            boolean hasMore = json.has("has_more") && json.get("has_more").getAsBoolean()
                    && json.has("next_cursor") && !json.get("next_cursor").isJsonNull();
            cursor = hasMore ? json.get("next_cursor").getAsString() : null;
            if (rows.size() >= MAX_FETCH) {
                log.warn("Notion database {} exceeded {} rows in LOCAL query mode; truncating", notion.databaseId(), MAX_FETCH);
                break;
            }
        } while (null != cursor);
        return rows;
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        EruptNotion notion = this.eruptNotion(eruptModel);
        JsonObject parent = new JsonObject();
        parent.addProperty("database_id", notion.databaseId());
        JsonObject body = new JsonObject();
        body.add("parent", parent);
        body.add("properties", this.encodeProperties(eruptModel, object, this.schema(notion)));
        this.request("POST", this.properties.getBaseUrl() + "/v1/pages", body.toString());
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        EruptNotion notion = this.eruptNotion(eruptModel);
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        JsonObject body = new JsonObject();
        body.add("properties", this.encodeProperties(eruptModel, object, this.schema(notion)));
        this.request("PATCH", this.pageUrl(id), body.toString());
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        // Notion has no hard delete; archiving removes the page from the database view
        JsonObject body = new JsonObject();
        body.addProperty("archived", true);
        this.request("PATCH", this.pageUrl(id), body.toString());
    }

    private EruptNotion eruptNotion(EruptModel eruptModel) {
        EruptNotion notion = eruptModel.getClazz().getAnnotation(EruptNotion.class);
        if (null == notion) {
            throw new EruptWebApiRuntimeException("@EruptNotion annotation is missing on " + eruptModel.getEruptName());
        }
        return notion;
    }

    private String pageUrl(Object pageId) {
        return this.properties.getBaseUrl() + "/v1/pages/" + pageId;
    }

    // --- schema (property name -> type), fetched once per database and cached ----------------

    private Map<String, String> schema(EruptNotion notion) {
        return this.schemaCache.computeIfAbsent(notion.databaseId(), databaseId -> {
            JsonObject json = GsonFactory.getGson().fromJson(
                    this.request("GET", this.properties.getBaseUrl() + "/v1/databases/" + databaseId, null), JsonObject.class);
            if (!json.has("properties") || !json.get("properties").isJsonObject()) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("notion.schema_failed"));
            }
            Map<String, String> types = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("properties").entrySet()) {
                types.put(entry.getKey(), entry.getValue().getAsJsonObject().get("type").getAsString());
            }
            return types;
        });
    }

    // --- property mapping (pure, unit-tested) -----------------------------------------------

    private Map<String, Object> decodeProperties(JsonObject properties) {
        Map<String, Object> row = new LinkedHashMap<>();
        if (null == properties) return row;
        for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
            row.put(entry.getKey(), decodeProperty(entry.getValue().getAsJsonObject()));
        }
        return row;
    }

    /**
     * Flatten one Notion property (a {@code {type, <type>: value}} wrapper) to a
     * plain scalar / list, unwrapping the common writable and read-only types and
     * falling back to a string for anything else.
     */
    static Object decodeProperty(JsonObject property) {
        if (!property.has("type")) return null;
        String type = property.get("type").getAsString();
        JsonElement value = property.get(type);
        if (null == value || value.isJsonNull()) return null;
        switch (type) {
            case "title":
            case "rich_text":
                return joinRichText(value.getAsJsonArray());
            case "number":
                return number(value);
            case "select":
            case "status":
                return value.getAsJsonObject().has("name") ? value.getAsJsonObject().get("name").getAsString() : null;
            case "multi_select": {
                List<Object> list = new ArrayList<>();
                for (JsonElement element : value.getAsJsonArray()) {
                    list.add(element.getAsJsonObject().get("name").getAsString());
                }
                return list;
            }
            case "checkbox":
                return value.getAsBoolean();
            case "date":
                return value.getAsJsonObject().has("start") && !value.getAsJsonObject().get("start").isJsonNull()
                        ? value.getAsJsonObject().get("start").getAsString() : null;
            case "url":
            case "email":
            case "phone_number":
            case "created_time":
            case "last_edited_time":
                return value.getAsString();
            case "people": {
                StringBuilder sb = new StringBuilder();
                for (JsonElement element : value.getAsJsonArray()) {
                    JsonObject person = element.getAsJsonObject();
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(person.has("name") ? person.get("name").getAsString() : person.get("id").getAsString());
                }
                return sb.toString();
            }
            default:
                return value.isJsonPrimitive() ? primitive(value.getAsJsonPrimitive()) : value.toString();
        }
    }

    /**
     * Wrap a plain value back into a Notion property body of the given schema type,
     * or {@code null} to skip (computed / unsupported types, or a null value).
     */
    static JsonObject encodeProperty(String type, JsonElement value) {
        if (null == type || null == value || value.isJsonNull()) return null;
        JsonObject property = new JsonObject();
        switch (type) {
            case "title":
            case "rich_text": {
                JsonObject content = new JsonObject();
                content.addProperty("content", value.getAsString());
                JsonObject segment = new JsonObject();
                segment.add("text", content);
                JsonArray array = new JsonArray();
                array.add(segment);
                property.add(type, array);
                return property;
            }
            case "number":
                property.add("number", value);
                return property;
            case "select":
            case "status": {
                JsonObject name = new JsonObject();
                name.addProperty("name", value.getAsString());
                property.add(type, name);
                return property;
            }
            case "multi_select": {
                JsonArray array = new JsonArray();
                if (value.isJsonArray()) {
                    for (JsonElement element : value.getAsJsonArray()) array.add(namedOption(element.getAsString()));
                } else {
                    array.add(namedOption(value.getAsString()));
                }
                property.add("multi_select", array);
                return property;
            }
            case "checkbox":
                property.addProperty("checkbox", value.getAsBoolean());
                return property;
            case "date": {
                JsonObject date = new JsonObject();
                date.addProperty("start", value.getAsString());
                property.add("date", date);
                return property;
            }
            case "url":
            case "email":
            case "phone_number":
                property.addProperty(type, value.getAsString());
                return property;
            default:
                // computed / relation / people / files ... are read-only here
                return null;
        }
    }

    private JsonObject encodeProperties(EruptModel eruptModel, Object object, Map<String, String> schema) {
        String primaryKey = eruptModel.getErupt().primaryKeyCol();
        JsonObject source = GsonFactory.getGson().toJsonTree(object).getAsJsonObject();
        JsonObject properties = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            if (entry.getKey().equals(primaryKey)) continue;
            JsonObject property = encodeProperty(schema.get(entry.getKey()), entry.getValue());
            if (null != property) properties.add(entry.getKey(), property);
        }
        return properties;
    }

    private static JsonObject namedOption(String name) {
        JsonObject option = new JsonObject();
        option.addProperty("name", name);
        return option;
    }

    private static String joinRichText(JsonArray array) {
        StringBuilder sb = new StringBuilder();
        for (JsonElement element : array) {
            JsonObject rich = element.getAsJsonObject();
            if (rich.has("plain_text")) {
                sb.append(rich.get("plain_text").getAsString());
            } else if (rich.has("text") && rich.getAsJsonObject("text").has("content")) {
                sb.append(rich.getAsJsonObject("text").get("content").getAsString());
            }
        }
        return sb.toString();
    }

    private static Object number(JsonElement value) {
        return value.isJsonPrimitive() ? primitive(value.getAsJsonPrimitive()) : null;
    }

    private static Object primitive(JsonPrimitive primitive) {
        if (primitive.isBoolean()) return primitive.getAsBoolean();
        if (primitive.isNumber()) {
            BigDecimal decimal = primitive.getAsBigDecimal();
            return decimal.stripTrailingZeros().scale() <= 0 ? (Object) decimal.longValue() : (Object) decimal.doubleValue();
        }
        return primitive.getAsString();
    }

    // --- transport --------------------------------------------------------------------------

    private String request(String method, String url, String body) {
        if (null == this.properties.getToken() || this.properties.getToken().trim().isEmpty()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("notion.token_missing"));
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + this.properties.getToken())
                .header("Notion-Version", this.properties.getVersion());
        if (null != body) builder.header("Content-Type", "application/json; charset=utf-8");
        builder.method(method, null == body ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("notion.request_failed")
                        + " → " + response.statusCode() + " " + this.errorMessage(response.body()));
            }
            return response.body();
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("notion.request_failed") + " → " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("notion.request_failed") + " → " + e.getMessage());
        }
    }

    // Notion error bodies carry a human-readable { "message": ... }
    private String errorMessage(String body) {
        try {
            JsonObject json = GsonFactory.getGson().fromJson(body, JsonObject.class);
            return null != json && json.has("message") ? json.get("message").getAsString() : "";
        } catch (RuntimeException ignore) {
            return "";
        }
    }

}
