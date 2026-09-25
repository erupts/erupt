package xyz.erupt.decision.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.decision.model.DecisionModel;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

/**
 * A provider reached over the System One wire protocol: one JSON post, one JSON body back.
 * Every hosted or self-hosted System One model speaks it, so a concrete provider only says
 * where it lives and whether a key is expected.
 *
 * @author YuePeng
 */
public abstract class HttpDecisionCore extends DecisionCore {

    // Pinned to HTTP/1.1: on plain http the client would otherwise open with an h2c upgrade
    // handshake, which uvicorn and friends answer to with an empty body
    private static final HttpClient HTTP = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10)).build();

    private static final String PATH = "/v1/systemone";

    // Rate limited and overloaded are the two the provider asks us to come back from
    private static final Set<Integer> RETRYABLE = Set.of(429, 529);

    private static final long BACKOFF_MS = 500;

    private static final long MAX_BACKOFF_MS = 8_000;

    /** Whether a blank key is a configuration error; a self-hosted model gets by without one */
    protected boolean keyRequired() {
        return true;
    }

    @Override
    protected JsonObject call(DecisionModel config, JsonObject request) {
        boolean keyed = StringUtils.isNotBlank(config.getApiKey());
        if (!keyed && this.keyRequired()) {
            throw new EruptWebApiRuntimeException("The decision model '" + config.getName() + "' has no API key");
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(endpoint(config.getApiUrl())))
                .timeout(Duration.ofSeconds(null == config.getTimeout() ? 15 : config.getTimeout()))
                .header("Content-Type", "application/json;charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(request.toString(), StandardCharsets.UTF_8));
        if (keyed) builder.header("Authorization", "Bearer " + config.getApiKey());
        HttpRequest httpRequest = builder.build();
        int attempts = (null == config.getRetries() ? 0 : Math.max(0, config.getRetries())) + 1;
        for (int attempt = 0; ; attempt++) {
            HttpResponse<String> response = send(httpRequest);
            if (response.statusCode() < 400) {
                return JsonParser.parseString(response.body()).getAsJsonObject();
            }
            if (attempt + 1 >= attempts || !RETRYABLE.contains(response.statusCode())) {
                // The body names the offending field on a 422; the key is never part of it
                throw new EruptWebApiRuntimeException("Decision HTTP " + response.statusCode() + ": " + response.body());
            }
            sleep(backoff(response, attempt));
        }
    }

    private static HttpResponse<String> send(HttpRequest request) {
        try {
            return HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EruptWebApiRuntimeException("The decision request was interrupted");
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException("The decision provider is unreachable: " + e.getMessage());
        }
    }

    // The provider's own retry-after wins; otherwise back off exponentially
    private static long backoff(HttpResponse<String> response, int attempt) {
        return response.headers().firstValue("retry-after")
                .map(it -> {
                    try {
                        return Math.max(0, (long) (Double.parseDouble(it.trim()) * 1000));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .orElse(Math.min(MAX_BACKOFF_MS, BACKOFF_MS << attempt));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EruptWebApiRuntimeException("The decision request was interrupted");
        }
    }

    static String endpoint(String apiUrl) {
        String url = StringUtils.removeEnd(StringUtils.trimToEmpty(apiUrl), "/");
        return url.endsWith(PATH) ? url : url + PATH;
    }

}
