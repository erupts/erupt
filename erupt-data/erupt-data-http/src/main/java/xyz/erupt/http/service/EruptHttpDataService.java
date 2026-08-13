package xyz.erupt.http.service;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.http.annotation.EruptHttp;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST-backed data source: models annotated with {@link EruptHttp} are read from and
 * written to a JSON endpoint. LOCAL query mode reuses the base class for
 * filtering / sorting / paging; REMOTE mode forwards paging to the endpoint.
 *
 * @author YuePeng
 */
@Service
public class EruptHttpDataService extends EruptBeanDataService<Map<String, Object>> {

    public static final String DATA_PROCESSOR = "HTTP";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptHttpDataService.class);
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL).build();

    private static final Type LIST_TYPE = new TypeToken<List<Map<String, Object>>>() {
    }.getType();

    @Override
    protected List<Map<String, Object>> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        EruptHttp eruptHttp = this.eruptHttp(eruptModel);
        return this.parseList(this.request(eruptHttp, "GET", eruptHttp.value(), null));
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        EruptHttp eruptHttp = this.eruptHttp(eruptModel);
        if (EruptHttp.QueryMode.LOCAL == eruptHttp.queryMode()) {
            return super.queryList(eruptModel, page, eruptQuery);
        }
        StringBuilder url = new StringBuilder(eruptHttp.value())
                .append(eruptHttp.value().contains("?") ? '&' : '?')
                .append("pageIndex=").append(page.getPageIndex())
                .append("&pageSize=").append(page.getPageSize());
        Optional.ofNullable(Sort.toSortString(page.getSort())).ifPresent(sort ->
                url.append("&sort=").append(this.encode(sort)));
        // only equality conditions translate cleanly to key=value query parameters
        for (Condition condition : this.mergeConditions(eruptQuery)) {
            if (QueryExpression.EQ == condition.getExpression()) {
                url.append('&').append(this.encode(condition.getKey()))
                        .append('=').append(this.encode(String.valueOf(condition.getValue())));
            }
        }
        String body = this.request(eruptHttp, "GET", url.toString(), null).trim();
        if (body.startsWith("[")) {
            List<Map<String, Object>> list = GsonFactory.getGson().fromJson(body, LIST_TYPE);
            page.setTotal((long) list.size());
            page.setList(list);
        } else {
            JsonObject json = GsonFactory.getGson().fromJson(body, JsonObject.class);
            page.setTotal(json.has("total") ? json.get("total").getAsLong() : 0L);
            page.setList(GsonFactory.getGson().fromJson(json.get("list"), LIST_TYPE));
        }
        return page;
    }

    // Returns a typed bean (not a map) because downstream drill / edit-form logic
    // reflects on the model class of the returned object
    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        EruptHttp eruptHttp = this.eruptHttp(eruptModel);
        String body = this.request(eruptHttp, "GET", this.idUrl(eruptHttp, id), null);
        return GsonFactory.getGson().fromJson(body, eruptModel.getClazz());
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        EruptHttp eruptHttp = this.eruptHttp(eruptModel);
        this.request(eruptHttp, "POST", eruptHttp.value(), GsonFactory.getGson().toJson(object));
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        EruptHttp eruptHttp = this.eruptHttp(eruptModel);
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        this.request(eruptHttp, "PUT", this.idUrl(eruptHttp, id), GsonFactory.getGson().toJson(object));
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        EruptHttp eruptHttp = this.eruptHttp(eruptModel);
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        this.request(eruptHttp, "DELETE", this.idUrl(eruptHttp, id), null);
    }

    private EruptHttp eruptHttp(EruptModel eruptModel) {
        EruptHttp eruptHttp = eruptModel.getClazz().getAnnotation(EruptHttp.class);
        if (null == eruptHttp) {
            throw new EruptWebApiRuntimeException("@EruptHttp annotation is missing on " + eruptModel.getEruptName());
        }
        return eruptHttp;
    }

    private String idUrl(EruptHttp eruptHttp, Object id) {
        String base = eruptHttp.value();
        return (base.endsWith("/") ? base.substring(0, base.length() - 1) : base)
                + "/" + this.encode(String.valueOf(id));
    }

    private List<Map<String, Object>> parseList(String body) {
        String trimmed = body.trim();
        if (trimmed.startsWith("[")) return GsonFactory.getGson().fromJson(trimmed, LIST_TYPE);
        JsonObject json = GsonFactory.getGson().fromJson(trimmed, JsonObject.class);
        if (json.has("list") && json.get("list").isJsonArray()) {
            return GsonFactory.getGson().fromJson(json.get("list"), LIST_TYPE);
        }
        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("http.unexpected_response"));
    }

    private String request(EruptHttp eruptHttp, String method, String url, String body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url))
                .timeout(Duration.ofSeconds(eruptHttp.timeout()));
        for (String header : eruptHttp.headers()) {
            int idx = header.indexOf(':');
            if (idx > 0) builder.header(header.substring(0, idx).trim(), header.substring(idx + 1).trim());
        }
        if (null != body) builder.header("Content-Type", "application/json");
        builder.method(method, null == body ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("http.request_failed")
                        + " → " + response.statusCode() + " " + method + " " + url);
            }
            return response.body();
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("http.request_failed") + " → " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("http.request_failed") + " → " + e.getMessage());
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
