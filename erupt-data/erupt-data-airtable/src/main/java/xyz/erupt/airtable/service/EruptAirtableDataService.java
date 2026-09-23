package xyz.erupt.airtable.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.erupt.airtable.annotation.EruptAirtable;
import xyz.erupt.airtable.prop.EruptAirtableProperties;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptRestTableDataService;
import xyz.erupt.core.view.EruptModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Airtable data source: models annotated with {@link EruptAirtable} are read from
 * and written to an Airtable table over the Web API ({@code /v0/{baseId}/{table}}).
 * Query mode is LOCAL — the whole table is fetched (offset-paged) and the base
 * class filters / sorts / pages it in memory. The token comes from
 * {@code erupt.airtable.token} and is sent as a bearer on every call. Each
 * record's {@code id} maps to the model primary key. Writes pass
 * {@code typecast: true} so plain strings land in select / date / linked fields.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class EruptAirtableDataService extends EruptRestTableDataService<EruptAirtable> {

    public static final String DATA_PROCESSOR = "AIRTABLE";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptAirtableDataService.class);
    }

    // Airtable pageSize ceiling
    private static final int PAGE_SIZE = 100;

    @Resource
    private EruptAirtableProperties properties;

    public EruptAirtableDataService() {
        super(EruptAirtable.class);
    }

    @Override
    protected String requestFailedMessage() {
        return I18nTranslate.$translate("airtable.request_failed");
    }

    @Override
    protected List<Map<String, Object>> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        EruptAirtable airtable = this.binding(eruptModel);
        List<Map<String, Object>> rows = new ArrayList<>();
        String offset = null;
        do {
            String url = this.tableUrl(airtable) + "?pageSize=" + PAGE_SIZE
                    + (null == offset ? "" : "&offset=" + encode(offset));
            JsonObject data = this.request("GET", url, null);
            for (JsonElement element : array(data, "records")) {
                JsonObject record = element.getAsJsonObject();
                rows.add(this.row(eruptModel, record.getAsJsonObject("fields"), string(record, "id")));
            }
            offset = string(data, "offset");
            if (rows.size() >= MAX_FETCH) {
                log.warn("Airtable {} exceeded {} rows in LOCAL query mode; truncating", airtable.table(), MAX_FETCH);
                break;
            }
        } while (null != offset);
        return rows;
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        this.request("POST", this.tableUrl(this.binding(eruptModel)), this.body(eruptModel, object));
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        // PATCH updates only the fields sent; PUT would clear every field left out
        this.request("PATCH", this.recordUrl(eruptModel, object), this.body(eruptModel, object));
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        this.request("DELETE", this.recordUrl(eruptModel, object), null);
    }

    private String body(EruptModel eruptModel, Object object) {
        JsonObject body = new JsonObject();
        body.add("fields", this.encodeFields(eruptModel, object));
        body.addProperty("typecast", true);
        return body.toString();
    }

    private String tableUrl(EruptAirtable airtable) {
        return this.properties.getBaseUrl() + "/v0/" + encode(airtable.baseId()) + "/" + encode(airtable.table());
    }

    private String recordUrl(EruptModel eruptModel, Object object) {
        return this.tableUrl(this.binding(eruptModel)) + "/" + encode(String.valueOf(this.id(eruptModel, object)));
    }

    // Airtable answers 2xx with the payload itself and 4xx with { error: { type, message } }
    private JsonObject request(String method, String url, String body) {
        if (isBlank(this.properties.getToken())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("airtable.config_missing"));
        }
        return this.send(method, url, body, "Authorization", "Bearer " + this.properties.getToken());
    }

}
