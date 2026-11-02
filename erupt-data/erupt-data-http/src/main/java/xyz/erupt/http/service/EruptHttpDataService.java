package xyz.erupt.http.service;

import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.http.EruptHttpAdapter;
import xyz.erupt.http.HttpTemplate;
import xyz.erupt.http.annotation.EruptHttp;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static xyz.erupt.annotation.query.Sort.toSortString;

/**
 * REST-backed data source: models annotated with {@link EruptHttp} are read from and
 * written to a JSON endpoint. LOCAL query mode reuses the base class for
 * filtering / sorting / paging; REMOTE mode forwards paging to the endpoint.
 * <p>
 * Every step that differs between APIs is a hook on {@link EruptHttpAdapter}; the URL
 * and headers are {@link HttpTemplate templates} resolved per request.
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

    private static final String GET = "GET", POST = "POST", DELETE = "DELETE";

    private static final String SORT = "sort";

    private static final EruptHttpAdapter DEFAULT_ADAPTER = new EruptHttpAdapter() {
    };

    private final Map<Class<? extends EruptHttpAdapter>, EruptHttpAdapter> adapters = new ConcurrentHashMap<>();

    @Override
    protected List<Map<String, Object>> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        Call call = this.call(eruptModel);
        return call.adapter.list(call.send(GET, call.baseUrl(), null), eruptModel);
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        Call call = this.call(eruptModel);
        if (EruptHttp.QueryMode.LOCAL == call.eruptHttp.queryMode()) {
            return super.queryList(eruptModel, page, eruptQuery);
        }
        Map<String, String> params = new LinkedHashMap<>();
        call.eruptHttp.paging().apply(params, page);
        Optional.ofNullable(toSortString(page.getSort())).ifPresent(sort -> params.put(SORT, sort));
        call.adapter.query(params, page, this.mergeConditions(eruptQuery), eruptModel);
        String base = call.baseUrl();
        String url = params.isEmpty() ? base : base + (base.contains("?") ? '&' : '?') + params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
        HttpResponse<String> response = call.send(GET, url, null);
        List<Map<String, Object>> list = call.adapter.list(response, eruptModel);
        page.setList(list);
        page.setTotal(Optional.ofNullable(call.adapter.total(response, eruptModel)).orElse((long) list.size()));
        return page;
    }

    // Returns a typed bean (not a map) because downstream drill / edit-form logic
    // reflects on the model class of the returned object
    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        Call call = this.call(eruptModel);
        return call.adapter.item(call.send(GET, call.idUrl(id), null), eruptModel);
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        Call call = this.call(eruptModel);
        call.send(POST, call.baseUrl(), call.adapter.body(object, POST, eruptModel));
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        Call call = this.call(eruptModel);
        String method = call.eruptHttp.editMethod().name();
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        call.send(method, call.idUrl(id), call.adapter.body(object, method, eruptModel));
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        Call call = this.call(eruptModel);
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        call.send(DELETE, call.idUrl(id), null);
    }

    public static EruptHttp eruptHttp(EruptModel eruptModel) {
        EruptHttp eruptHttp = eruptModel.getClazz().getAnnotation(EruptHttp.class);
        if (null == eruptHttp) {
            throw new EruptWebApiRuntimeException("@EruptHttp annotation is missing on " + eruptModel.getEruptName());
        }
        return eruptHttp;
    }

    private Call call(EruptModel eruptModel) {
        EruptHttp eruptHttp = eruptHttp(eruptModel);
        Class<? extends EruptHttpAdapter> clazz = eruptHttp.adapter();
        EruptHttpAdapter adapter = EruptHttpAdapter.class == clazz ? DEFAULT_ADAPTER
                : adapters.computeIfAbsent(clazz, EruptSpringUtil::getBean);
        return new Call(eruptModel, eruptHttp, adapter);
    }

    static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * One model's resolved binding: the annotation, its adapter, and the transport.
     */
    private record Call(EruptModel model, EruptHttp eruptHttp, EruptHttpAdapter adapter) {

        String baseUrl() {
            return adapter.url(HttpTemplate.resolve(eruptHttp.value()), model);
        }

        String idUrl(Object id) {
            String base = HttpTemplate.resolve(eruptHttp.value());
            String url = (base.endsWith("/") ? base.substring(0, base.length() - 1) : base) + "/" + encode(String.valueOf(id));
            return adapter.url(url, model);
        }

        HttpResponse<String> send(String method, String url, String body) {
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url))
                    .timeout(Duration.ofSeconds(eruptHttp.timeout()));
            for (String header : eruptHttp.headers()) {
                int idx = header.indexOf(':');
                if (idx > 0) builder.header(header.substring(0, idx).trim(), HttpTemplate.resolve(header.substring(idx + 1).trim()));
            }
            if (null != body) builder.header("Content-Type", "application/json");
            adapter.request(builder, model);
            builder.method(method, null == body ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            try {
                HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new EruptWebApiRuntimeException(I18nTranslate.$translate("http.request_failed")
                            + " → " + adapter.error(response, method, url));
                }
                return response;
            } catch (IOException e) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("http.request_failed") + " → " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("http.request_failed") + " → " + e.getMessage());
            }
        }
    }

}
