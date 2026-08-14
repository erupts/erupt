package xyz.erupt.feishu.service;

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
import xyz.erupt.feishu.annotation.EruptFeishu;
import xyz.erupt.feishu.prop.EruptFeishuProperties;

import java.io.IOException;
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
 * Feishu Bitable (多维表格) data source: models annotated with {@link EruptFeishu}
 * are read from and written to a Bitable table over the open-platform REST API.
 * Query mode is LOCAL — the whole table is fetched (cursor-paged) and the base
 * class filters / sorts / pages it in memory, so this class only supplies
 * {@link #data} plus real CRUD. Credentials come from {@code erupt.feishu.*}; a
 * tenant access token is fetched on demand and cached until shortly before it
 * expires. Each record's {@code record_id} maps to the model primary key.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class EruptFeishuDataService extends EruptBeanDataService<Map<String, Object>> {

    public static final String DATA_PROCESSOR = "FEISHU_BITABLE";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptFeishuDataService.class);
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL).build();

    // Bitable page_size ceiling; and a guard so a huge table cannot exhaust memory
    private static final int PAGE_SIZE = 500;

    private static final int MAX_FETCH = 5000;

    @Resource
    private EruptFeishuProperties properties;

    private volatile String cachedToken;

    private volatile long tokenExpireAt;

    @Override
    protected List<Map<String, Object>> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        EruptFeishu feishu = this.eruptFeishu(eruptModel);
        String primaryKey = eruptModel.getErupt().primaryKeyCol();
        List<Map<String, Object>> rows = new ArrayList<>();
        String pageToken = null;
        do {
            // Use the search endpoint; the legacy GET list-records API is deprecated
            // and returns 400 on newer bases. Empty body = fetch all fields, no filter.
            String url = this.recordsUrl(feishu) + "/search?page_size=" + PAGE_SIZE
                    + (null == pageToken ? "" : "&page_token=" + this.encode(pageToken));
            JsonObject data = this.okData(this.request("POST", url, "{}"));
            JsonArray items = data.has("items") && data.get("items").isJsonArray()
                    ? data.getAsJsonArray("items") : new JsonArray();
            for (JsonElement element : items) {
                JsonObject item = element.getAsJsonObject();
                Map<String, Object> row = this.decodeFields(item.getAsJsonObject("fields"));
                row.put(primaryKey, item.get("record_id").getAsString());
                rows.add(row);
            }
            boolean hasMore = data.has("has_more") && data.get("has_more").getAsBoolean() && data.has("page_token");
            pageToken = hasMore ? data.get("page_token").getAsString() : null;
            if (rows.size() >= MAX_FETCH) {
                log.warn("Feishu Bitable {} exceeded {} rows in LOCAL query mode; truncating", feishu.tableId(), MAX_FETCH);
                break;
            }
        } while (null != pageToken);
        return rows;
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        EruptFeishu feishu = this.eruptFeishu(eruptModel);
        JsonObject body = new JsonObject();
        body.add("fields", this.encodeFields(eruptModel, object));
        this.okData(this.request("POST", this.recordsUrl(feishu), body.toString()));
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        EruptFeishu feishu = this.eruptFeishu(eruptModel);
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        JsonObject body = new JsonObject();
        body.add("fields", this.encodeFields(eruptModel, object));
        this.okData(this.request("PUT", this.recordUrl(feishu, id), body.toString()));
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        EruptFeishu feishu = this.eruptFeishu(eruptModel);
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        this.okData(this.request("DELETE", this.recordUrl(feishu, id), null));
    }

    private EruptFeishu eruptFeishu(EruptModel eruptModel) {
        EruptFeishu feishu = eruptModel.getClazz().getAnnotation(EruptFeishu.class);
        if (null == feishu) {
            throw new EruptWebApiRuntimeException("@EruptFeishu annotation is missing on " + eruptModel.getEruptName());
        }
        return feishu;
    }

    private String recordsUrl(EruptFeishu feishu) {
        return this.properties.getBaseUrl() + "/open-apis/bitable/v1/apps/"
                + feishu.baseToken() + "/tables/" + feishu.tableId() + "/records";
    }

    private String recordUrl(EruptFeishu feishu, Object recordId) {
        return this.recordsUrl(feishu) + "/" + this.encode(String.valueOf(recordId));
    }

    // --- field mapping (pure, unit-tested) --------------------------------------------------

    private Map<String, Object> decodeFields(JsonObject fields) {
        Map<String, Object> row = new LinkedHashMap<>();
        if (null == fields) return row;
        for (Map.Entry<String, JsonElement> entry : fields.entrySet()) {
            row.put(entry.getKey(), fieldValue(entry.getValue()));
        }
        return row;
    }

    /**
     * Flatten a Bitable field value to plain Java: primitives as clean scalars,
     * multi-select (array of primitives) as a list, and rich text / person / link
     * (arrays or objects carrying {@code text} / {@code name} / {@code link}) as a
     * joined string.
     */
    static Object fieldValue(JsonElement element) {
        if (null == element || element.isJsonNull()) return null;
        if (element.isJsonPrimitive()) return primitive(element.getAsJsonPrimitive());
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() > 0 && array.get(0).isJsonObject()) {
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
            for (String key : new String[]{"text", "name", "en_name", "link"}) {
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

    // Bitable write accepts scalar / array values as-is; drop the primary key and nulls
    private JsonObject encodeFields(EruptModel eruptModel, Object object) {
        String primaryKey = eruptModel.getErupt().primaryKeyCol();
        JsonObject source = GsonFactory.getGson().toJsonTree(object).getAsJsonObject();
        JsonObject fields = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            if (entry.getKey().equals(primaryKey) || null == entry.getValue() || entry.getValue().isJsonNull()) continue;
            fields.add(entry.getKey(), entry.getValue());
        }
        return fields;
    }

    // --- token & transport ------------------------------------------------------------------

    private synchronized String tenantToken() {
        long now = System.currentTimeMillis();
        if (null != this.cachedToken && now < this.tokenExpireAt) return this.cachedToken;
        if (this.isBlank(this.properties.getAppId()) || this.isBlank(this.properties.getAppSecret())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("feishu.config_missing"));
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("app_id", this.properties.getAppId());
        payload.addProperty("app_secret", this.properties.getAppSecret());
        String url = this.properties.getBaseUrl() + "/open-apis/auth/v3/tenant_access_token/internal";
        JsonObject json = GsonFactory.getGson().fromJson(
                this.rawRequest("POST", url, payload.toString(), null), JsonObject.class);
        if (0 != json.get("code").getAsInt()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("feishu.auth_failed") + " → " + json.get("msg").getAsString());
        }
        this.cachedToken = json.get("tenant_access_token").getAsString();
        // refresh a minute early to avoid using a token that expires mid-flight
        this.tokenExpireAt = System.currentTimeMillis() + (json.get("expire").getAsLong() - 60) * 1000L;
        return this.cachedToken;
    }

    // Feishu returns HTTP 200 with a { code, msg, data } envelope; unwrap data or raise msg
    private JsonObject okData(String response) {
        JsonObject json = GsonFactory.getGson().fromJson(response, JsonObject.class);
        if (0 != json.get("code").getAsInt()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("feishu.request_failed") + " → " + json.get("msg").getAsString());
        }
        return json.has("data") && json.get("data").isJsonObject() ? json.getAsJsonObject("data") : new JsonObject();
    }

    private String request(String method, String url, String body) {
        return this.rawRequest(method, url, body, this.tenantToken());
    }

    private String rawRequest(String method, String url, String body, String bearer) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(15));
        if (null != bearer) builder.header("Authorization", "Bearer " + bearer);
        if (null != body) builder.header("Content-Type", "application/json; charset=utf-8");
        builder.method(method, null == body ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                // Feishu returns a { code, msg } envelope even on 4xx; surface it so the
                // real cause (invalid token / field / page_size) is visible, not just the status.
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("feishu.request_failed")
                        + " → " + response.statusCode() + " " + method + " " + url
                        + (this.isBlank(response.body()) ? "" : " " + response.body()));
            }
            return response.body();
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("feishu.request_failed") + " → " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("feishu.request_failed") + " → " + e.getMessage());
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return null == value || value.trim().isEmpty();
    }

}
