package xyz.erupt.ai_staff.channel;

import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * IM channel abstraction for AI staff. Implementations self-register on
 * construction (same registry idiom as LlmCore), covering two directions:
 * outbound push (work reports) and inbound bot conversation (callbacks).
 *
 * @author YuePeng
 * date 2026/8/3
 */
public abstract class StaffChannel {

    private static final Map<String, StaffChannel> channels = new HashMap<>();

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public StaffChannel() {
        channels.put(this.code(), this);
    }

    public static StaffChannel get(String code) {
        return channels.get(code);
    }

    public abstract String code();

    // Config template pre-filled into the channel config editor when the type is chosen
    public abstract Object configTemplate();

    // Proactively push a message to the channel (work reports, notifications)
    public abstract void push(JsonObject config, String content);

    /**
     * Silently verify the channel's API credentials (no visible message sent);
     * throws on failure, returns false when nothing verifiable is configured.
     */
    public abstract boolean testConnect(JsonObject config);

    /**
     * Handle a platform callback (signature check, handshake, message parsing).
     * Returns the body to answer the platform immediately; each recognized user
     * message is handed to the listener and replied asynchronously via {@link #reply}.
     */
    public abstract String onCallback(JsonObject config, ChannelRequest request, Consumer<ChannelMessage> listener);

    // Send the answer for a message received via onCallback
    public abstract void reply(JsonObject config, ChannelMessage message, String content);

    // ======================== shared helpers ========================

    protected String str(JsonObject config, String key) {
        return config.has(key) && !config.get(key).isJsonNull() ? config.get(key).getAsString() : null;
    }

    protected String post(String url, String json) {
        return this.post(url, json, Map.of());
    }

    @SneakyThrows
    protected String post(String url, String json, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json;charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
        headers.forEach(builder::header);
        HttpResponse<String> response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new EruptWebApiRuntimeException("Channel HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    @SneakyThrows
    protected String httpGet(String url) {
        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder().uri(URI.create(url))
                .timeout(Duration.ofSeconds(30)).GET().build(), HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    @SneakyThrows
    protected byte[] hmacSha256(String key, String data) {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    protected String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static class H implements ChoiceFetchHandler<Void> {
        @Override
        public List<VLModel> fetch(String[] params) {
            return channels.keySet().stream().map(it -> new VLModel(it, it))
                    .sorted(Comparator.comparing(VLModel::getLabel)).collect(Collectors.toList());
        }
    }

}
