package xyz.erupt.decision.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
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
 * TypeSafe's Jev, the first System One model.
 *
 * @author YuePeng
 */
@Component
public class Jev extends DecisionCore {

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private static final String PATH = "/v1/systemone";

    // Rate limited and overloaded are the two the provider asks us to come back from
    private static final Set<Integer> RETRYABLE = Set.of(429, 529);

    private static final long BACKOFF_MS = 500;

    private static final long MAX_BACKOFF_MS = 8_000;

    @Override
    public String code() {
        return "TypeSafe Jev";
    }

    @Override
    public String api() {
        return "https://api.typesafe.ai";
    }

    @Override
    public String model() {
        return "jev-latest";
    }

    @Override
    protected JsonObject call(DecisionModel config, JsonObject request) {
        if (StringUtils.isBlank(config.getApiKey())) {
            throw new EruptWebApiRuntimeException("The decision model '" + config.getName() + "' has no API key");
        }
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(endpoint(config.getApiUrl())))
                .timeout(Duration.ofSeconds(null == config.getTimeout() ? 15 : config.getTimeout()))
                .header("Content-Type", "application/json;charset=utf-8")
                .header("Authorization", "Bearer " + config.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(request.toString(), StandardCharsets.UTF_8))
                .build();
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

    private static String endpoint(String apiUrl) {
        String url = StringUtils.removeEnd(StringUtils.trimToEmpty(apiUrl), "/");
        return url.endsWith(PATH) ? url : url + PATH;
    }

}
