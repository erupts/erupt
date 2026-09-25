package xyz.erupt.dingtalk.service;

import com.google.gson.JsonArray;
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
import xyz.erupt.dingtalk.annotation.EruptDingTalk;
import xyz.erupt.dingtalk.prop.EruptDingTalkProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DingTalk Notable data source: models annotated with
 * {@link EruptDingTalk} are read from and written to a Notable sheet over the
 * open-platform REST API ({@code /v1.0/notable/...}). Query mode is LOCAL — the
 * whole sheet is fetched (cursor-paged) and the base class filters / sorts /
 * pages it in memory. Credentials come from {@code erupt.dingtalk.*}; an access
 * token is fetched on demand and cached until shortly before it expires. Every
 * Notable call is made on behalf of an operator (union id), taken from the
 * annotation or the properties. Each record's {@code id} maps to the model
 * primary key.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class EruptDingTalkDataService extends EruptRestTableDataService<EruptDingTalk> {

    public static final String DATA_PROCESSOR = "DINGTALK_NOTABLE";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptDingTalkDataService.class);
    }

    private static final String TOKEN_HEADER = "x-acs-dingtalk-access-token";

    // Notable maxResults ceiling
    private static final int PAGE_SIZE = 100;

    @Resource
    private EruptDingTalkProperties properties;

    private volatile String cachedToken;

    private volatile long tokenExpireAt;

    public EruptDingTalkDataService() {
        super(EruptDingTalk.class);
    }

    @Override
    protected String requestFailedMessage() {
        return I18nTranslate.$translate("dingtalk.request_failed");
    }

    @Override
    protected List<Map<String, Object>> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        EruptDingTalk dingTalk = this.binding(eruptModel);
        List<Map<String, Object>> rows = new ArrayList<>();
        String nextToken = null;
        do {
            JsonObject body = new JsonObject();
            body.addProperty("maxResults", PAGE_SIZE);
            if (null != nextToken) body.addProperty("nextToken", nextToken);
            JsonObject data = this.request("POST", this.recordsUrl(dingTalk, "/list"), body.toString());
            for (JsonElement element : array(data, "records")) {
                JsonObject record = element.getAsJsonObject();
                rows.add(this.row(eruptModel, record.getAsJsonObject("fields"), string(record, "id")));
            }
            boolean hasMore = data.has("hasMore") && data.get("hasMore").getAsBoolean();
            nextToken = hasMore ? string(data, "nextToken") : null;
            if (rows.size() >= MAX_FETCH) {
                log.warn("DingTalk Notable {} exceeded {} rows in LOCAL query mode; truncating", dingTalk.sheet(), MAX_FETCH);
                break;
            }
        } while (null != nextToken);
        return rows;
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        JsonObject record = new JsonObject();
        record.add("fields", this.encodeFields(eruptModel, object));
        this.request("POST", this.recordsUrl(this.binding(eruptModel), ""), this.records(record).toString());
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        JsonObject record = new JsonObject();
        record.addProperty("id", String.valueOf(this.id(eruptModel, object)));
        record.add("fields", this.encodeFields(eruptModel, object));
        this.request("PUT", this.recordsUrl(this.binding(eruptModel), ""), this.records(record).toString());
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        JsonArray ids = new JsonArray();
        ids.add(String.valueOf(this.id(eruptModel, object)));
        JsonObject body = new JsonObject();
        body.add("recordIds", ids);
        this.request("POST", this.recordsUrl(this.binding(eruptModel), "/delete"), body.toString());
    }

    // Notable write endpoints are batch-shaped: { records: [ ... ] }
    private JsonObject records(JsonObject record) {
        JsonArray records = new JsonArray();
        records.add(record);
        JsonObject body = new JsonObject();
        body.add("records", records);
        return body;
    }

    private String recordsUrl(EruptDingTalk dingTalk, String suffix) {
        String operatorId = isBlank(dingTalk.operatorId()) ? this.properties.getOperatorId() : dingTalk.operatorId();
        if (isBlank(operatorId)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("dingtalk.operator_missing"));
        }
        return this.properties.getBaseUrl() + "/v1.0/notable/bases/" + encode(dingTalk.baseId())
                + "/sheets/" + encode(dingTalk.sheet()) + "/records" + suffix + "?operatorId=" + encode(operatorId);
    }

    // --- token ------------------------------------------------------------------------------

    private synchronized String accessToken() {
        long now = System.currentTimeMillis();
        if (null != this.cachedToken && now < this.tokenExpireAt) return this.cachedToken;
        if (isBlank(this.properties.getClientId()) || isBlank(this.properties.getClientSecret())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("dingtalk.config_missing"));
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("appKey", this.properties.getClientId());
        payload.addProperty("appSecret", this.properties.getClientSecret());
        JsonObject json = this.send("POST", this.properties.getBaseUrl() + "/v1.0/oauth2/accessToken", payload.toString());
        this.cachedToken = string(json, "accessToken");
        if (isBlank(this.cachedToken)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("dingtalk.auth_failed") + " → " + json);
        }
        // refresh a minute early to avoid using a token that expires mid-flight
        this.tokenExpireAt = System.currentTimeMillis() + (json.get("expireIn").getAsLong() - 60) * 1000L;
        return this.cachedToken;
    }

    // DingTalk v1.0 APIs answer 2xx with the payload itself and 4xx with { code, message }
    private JsonObject request(String method, String url, String body) {
        return this.send(method, url, body, TOKEN_HEADER, this.accessToken());
    }

}
