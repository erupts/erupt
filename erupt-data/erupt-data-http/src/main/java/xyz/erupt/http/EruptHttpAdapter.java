package xyz.erupt.http;

import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.http.annotation.EruptHttp;
import xyz.erupt.http.service.EruptHttpDataService;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Per-model hooks of the REST data source, in the order a request goes through them.
 * Every method has a default, so an adapter overrides only what its API does differently:
 * sign a request, name the paging parameters, unwrap an envelope, surface the remote
 * error message. Bind it with {@link EruptHttp#adapter()}.
 * <p>
 * Templates in {@code value} / {@code headers} are resolved before these hooks run, so
 * {@link #url} and {@link #request} see final strings.
 *
 * @author YuePeng
 */
public interface EruptHttpAdapter {

    /**
     * Final URL of a request: rewrite path segments, add a tenant prefix, switch hosts.
     */
    default String url(String url, EruptModel model) {
        return url;
    }

    /**
     * Called before every request is sent: authentication headers, HMAC signatures,
     * per-user tokens read from {@code MetaContext}.
     */
    default void request(HttpRequest.Builder request, EruptModel model) {
    }

    /**
     * REMOTE mode: encode the query into parameters. {@code params} already holds the
     * {@link EruptHttp.Paging} pair, {@code sort} and one entry per equality condition;
     * amend or replace them here, e.g. {@code name_like} for LIKE or a {@code filter}
     * expression. Values are URL-encoded afterwards.
     */
    default void query(Map<String, String> params, Page page, List<Condition> conditions, EruptModel model) {
        for (Condition condition : conditions) {
            if (QueryExpression.EQ == condition.getExpression()) {
                params.put(condition.getKey(), String.valueOf(condition.getValue()));
            }
        }
    }

    /**
     * Request body for add / edit: wrap in an envelope, drop the id on POST, rename fields.
     */
    default String body(Object object, String method, EruptModel model) {
        return GsonFactory.getGson().toJson(object);
    }

    /**
     * Records of a list response (LOCAL mode fetch and REMOTE page). The default reads
     * {@link EruptHttp#listPath()}.
     */
    default List<Map<String, Object>> list(HttpResponse<String> response, EruptModel model) {
        return HttpEnvelope.list(response.body(), EruptHttpDataService.eruptHttp(model).listPath());
    }

    /**
     * Total record count of a REMOTE page, or {@code null} when the response carries
     * none (the page size is used). The default reads {@link EruptHttp#totalPath()};
     * override for a count in a header such as {@code X-Total-Count}.
     */
    default Long total(HttpResponse<String> response, EruptModel model) {
        return HttpEnvelope.total(response.body(), EruptHttpDataService.eruptHttp(model).totalPath());
    }

    /**
     * The record of a single-object response, as an instance of the model class. The
     * default reads {@link EruptHttp#itemPath()}.
     */
    default Object item(HttpResponse<String> response, EruptModel model) {
        return HttpEnvelope.item(response.body(), EruptHttpDataService.eruptHttp(model).itemPath(), model.getClazz());
    }

    /**
     * Message shown to the user for a non-2xx response. The default appends the status
     * and any {@code message} / {@code msg} / {@code error} string found in the body.
     */
    default String error(HttpResponse<String> response, String method, String url) {
        String detail = HttpEnvelope.message(response.body());
        return response.statusCode() + " " + method + " " + url + (null == detail ? "" : " → " + detail);
    }

}
