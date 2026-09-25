package xyz.erupt.feishu.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptRestTableDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.feishu.annotation.EruptFeishu;
import xyz.erupt.feishu.prop.EruptFeishuProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Feishu Bitable data source: models annotated with {@link EruptFeishu}
 * are read from and written to a Bitable table over the open-platform REST API.
 * Query mode is LOCAL — the whole table is fetched (cursor-paged) and the base
 * class filters / sorts / pages it in memory. Credentials come from
 * {@code erupt.feishu.*}; a tenant access token is fetched on demand and cached
 * until shortly before it expires. Each record's {@code record_id} maps to the
 * model primary key.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class EruptFeishuDataService extends EruptRestTableDataService<EruptFeishu> {

    public static final String DATA_PROCESSOR = "FEISHU_BITABLE";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptFeishuDataService.class);
    }

    // Bitable page_size ceiling
    private static final int PAGE_SIZE = 500;

    @Resource
    private EruptFeishuProperties properties;

    private volatile String cachedToken;

    private volatile long tokenExpireAt;

    public EruptFeishuDataService() {
        super(EruptFeishu.class);
    }

    @Override
    protected String requestFailedMessage() {
        return I18nTranslate.$translate("feishu.request_failed");
    }

    @Override
    protected List<Map<String, Object>> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        EruptFeishu feishu = this.binding(eruptModel);
        List<Map<String, Object>> rows = new ArrayList<>();
        String pageToken = null;
        do {
            // Use the search endpoint; the legacy GET list-records API is deprecated
            // and returns 400 on newer bases. Empty body = fetch all fields, no filter.
            String url = this.recordsUrl(feishu) + "/search?page_size=" + PAGE_SIZE
                    + (null == pageToken ? "" : "&page_token=" + encode(pageToken));
            JsonObject data = this.okData(this.request("POST", url, "{}"));
            for (JsonElement element : array(data, "items")) {
                JsonObject item = element.getAsJsonObject();
                rows.add(this.row(eruptModel, item.getAsJsonObject("fields"), string(item, "record_id")));
            }
            boolean hasMore = data.has("has_more") && data.get("has_more").getAsBoolean();
            pageToken = hasMore ? string(data, "page_token") : null;
            if (rows.size() >= MAX_FETCH) {
                log.warn("Feishu Bitable {} exceeded {} rows in LOCAL query mode; truncating", feishu.tableId(), MAX_FETCH);
                break;
            }
        } while (null != pageToken);
        return rows;
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        JsonObject body = new JsonObject();
        body.add("fields", this.encodeFields(eruptModel, object));
        this.okData(this.request("POST", this.recordsUrl(this.binding(eruptModel)), body.toString()));
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        JsonObject body = new JsonObject();
        body.add("fields", this.encodeFields(eruptModel, object));
        this.okData(this.request("PUT", this.recordUrl(this.binding(eruptModel), this.id(eruptModel, object)), body.toString()));
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        this.okData(this.request("DELETE", this.recordUrl(this.binding(eruptModel), this.id(eruptModel, object)), null));
    }

    private String recordsUrl(EruptFeishu feishu) {
        return this.properties.getBaseUrl() + "/open-apis/bitable/v1/apps/"
                + feishu.baseToken() + "/tables/" + feishu.tableId() + "/records";
    }

    private String recordUrl(EruptFeishu feishu, Object recordId) {
        return this.recordsUrl(feishu) + "/" + encode(String.valueOf(recordId));
    }

    // --- token & envelope -------------------------------------------------------------------

    private synchronized String tenantToken() {
        long now = System.currentTimeMillis();
        if (null != this.cachedToken && now < this.tokenExpireAt) return this.cachedToken;
        if (isBlank(this.properties.getAppId()) || isBlank(this.properties.getAppSecret())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("feishu.config_missing"));
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("app_id", this.properties.getAppId());
        payload.addProperty("app_secret", this.properties.getAppSecret());
        JsonObject json = this.send("POST", this.properties.getBaseUrl() + "/open-apis/auth/v3/tenant_access_token/internal", payload.toString());
        if (0 != json.get("code").getAsInt()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("feishu.auth_failed") + " → " + string(json, "msg"));
        }
        this.cachedToken = string(json, "tenant_access_token");
        // refresh a minute early to avoid using a token that expires mid-flight
        this.tokenExpireAt = System.currentTimeMillis() + (json.get("expire").getAsLong() - 60) * 1000L;
        return this.cachedToken;
    }

    // Feishu returns HTTP 200 with a { code, msg, data } envelope; unwrap data or raise msg
    private JsonObject okData(JsonObject json) {
        if (0 != json.get("code").getAsInt()) {
            throw new EruptWebApiRuntimeException(this.requestFailedMessage() + " → " + string(json, "msg"));
        }
        return json.has("data") && json.get("data").isJsonObject() ? json.getAsJsonObject("data") : new JsonObject();
    }

    private JsonObject request(String method, String url, String body) {
        return this.send(method, url, body, "Authorization", "Bearer " + this.tenantToken());
    }

}
