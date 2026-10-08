package xyz.erupt.ai_tune.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.ai_tune.model.TuneJob;
import xyz.erupt.ai_tune.prop.TuneProp;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.EruptSpringUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * A fine-tuning backend. One adapter serves every LLM provider code it names, so the job
 * picks its backend from the base model's LLM record and no second credential is needed.
 * Adapters register themselves on construction, mirroring {@code LlmCore}.
 *
 * @author YuePeng
 * date 2026/10/8
 */
public abstract class TuneCore {

    private static final Map<String, TuneCore> registry = new HashMap<>();

    // Pinned to HTTP/1.1: on plain http the client would otherwise open with an h2c upgrade
    // handshake that uvicorn-style trainers answer with an empty body
    private static final HttpClient HTTP = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(15)).build();

    protected TuneCore() {
        for (String code : this.codes()) {
            registry.put(code, this);
        }
    }

    public static TuneCore get(String llmCode) {
        return null == llmCode ? null : registry.get(llmCode);
    }

    public static Set<String> supportedCodes() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    /** LLM provider codes ({@code LLM.llm}) this backend trains for */
    public abstract String[] codes();

    /** Uploads one JSONL file and returns the provider's file id */
    public abstract String uploadFile(TuneJob job, String fileName, byte[] jsonl, boolean validation);

    /** Submits the job and returns the provider's job id */
    public abstract String createJob(TuneJob job);

    public abstract TuneJobState retrieve(TuneJob job);

    /**
     * Events not yet mirrored locally. {@code knownIds} holds the remote ids already stored,
     * so a paginating adapter can stop as soon as it reaches them.
     */
    public abstract List<TuneEventVo> events(TuneJob job, Set<String> knownIds);

    public List<TuneCheckpointVo> checkpoints(TuneJob job) {
        return List.of();
    }

    public abstract void cancel(TuneJob job);

    // ---------------------------------------------------------------- HTTP helpers

    protected String baseUrl(TuneJob job) {
        return StringUtils.removeEnd(StringUtils.trimToEmpty(job.getBaseLlm().getApiUrl()), "/");
    }

    protected JsonObject getJson(TuneJob job, String url) {
        return send(job, HttpRequest.newBuilder().uri(URI.create(url)).GET());
    }

    protected JsonObject postJson(TuneJob job, String url, JsonObject body) {
        return send(job, HttpRequest.newBuilder().uri(URI.create(url))
                .header("Content-Type", "application/json;charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8)));
    }

    protected JsonObject postEmpty(TuneJob job, String url) {
        return send(job, HttpRequest.newBuilder().uri(URI.create(url)).POST(HttpRequest.BodyPublishers.noBody()));
    }

    protected JsonObject postMultipart(TuneJob job, String url, Map<String, String> fields,
                                       String fileField, String fileName, byte[] bytes) {
        String boundary = "----erupt" + UUID.randomUUID().toString().replace("-", "");
        ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length + 1024);
        try {
            for (Map.Entry<String, String> field : fields.entrySet()) {
                out.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + field.getKey()
                        + "\"\r\n\r\n" + field.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
            }
            out.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + fileField
                    + "\"; filename=\"" + fileName + "\"\r\nContent-Type: application/jsonl\r\n\r\n")
                    .getBytes(StandardCharsets.UTF_8));
            out.write(bytes);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
        return send(job, HttpRequest.newBuilder().uri(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray())));
    }

    private JsonObject send(TuneJob job, HttpRequest.Builder builder) {
        builder.timeout(EruptSpringUtil.getBean(TuneProp.class).getRequestTimeout());
        if (StringUtils.isNotBlank(job.getBaseLlm().getApiKey())) {
            builder.header("Authorization", "Bearer " + job.getBaseLlm().getApiKey());
        }
        HttpResponse<String> response;
        try {
            response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EruptWebApiRuntimeException("Fine-tuning request interrupted");
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException("Fine-tuning provider unreachable: " + e.getMessage());
        }
        if (response.statusCode() >= 400) {
            // Provider error bodies name the offending field; the key never appears in them
            throw new EruptWebApiRuntimeException("Fine-tuning HTTP " + response.statusCode() + ": "
                    + StringUtils.abbreviate(response.body(), 1000));
        }
        String body = response.body();
        if (StringUtils.isBlank(body)) return new JsonObject();
        JsonElement element = JsonParser.parseString(body);
        return element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
    }

    // ---------------------------------------------------------------- JSON helpers

    protected static String str(JsonObject obj, String key) {
        if (null == obj || !obj.has(key) || obj.get(key).isJsonNull()) return null;
        JsonElement el = obj.get(key);
        return el.isJsonPrimitive() ? el.getAsString() : el.toString();
    }

    protected static Long lng(JsonObject obj, String key) {
        if (null == obj || !obj.has(key) || obj.get(key).isJsonNull() || !obj.get(key).isJsonPrimitive()) return null;
        try {
            return obj.get(key).getAsLong();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected static Integer integer(JsonObject obj, String key) {
        Long value = lng(obj, key);
        return null == value ? null : value.intValue();
    }

    protected static Double dbl(JsonObject obj, String key) {
        if (null == obj || !obj.has(key) || obj.get(key).isJsonNull() || !obj.get(key).isJsonPrimitive()) return null;
        try {
            double d = obj.get(key).getAsDouble();
            return Double.isNaN(d) ? null : d;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected static JsonObject obj(JsonObject obj, String key) {
        return null != obj && obj.has(key) && obj.get(key).isJsonObject() ? obj.getAsJsonObject(key) : null;
    }

    protected static LocalDateTime epochSeconds(Long seconds) {
        return null == seconds || seconds <= 0 ? null
                : LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault());
    }

    protected static Map<String, Double> numericFields(JsonObject obj) {
        Map<String, Double> metrics = new LinkedHashMap<>();
        if (null == obj) return metrics;
        for (String key : obj.keySet()) {
            Double value = dbl(obj, key);
            if (null != value) metrics.put(key, value);
        }
        return metrics;
    }

}
