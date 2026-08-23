package xyz.erupt.ai_staff.channel.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_staff.channel.ChannelMessage;
import xyz.erupt.ai_staff.channel.ChannelRequest;
import xyz.erupt.ai_staff.channel.StaffChannel;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Feishu (Lark): push via custom bot webhook (optional signed), inbound via
 * event subscription v2 (im.message.receive_v1, optionally AES-encrypted) —
 * replies via the IM API with a tenant access token.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Slf4j
@Component
public class FeishuChannel extends StaffChannel {

    private static final String TENANT_TOKEN_API = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";

    private static final String MESSAGE_API = "https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=chat_id";

    @Override
    public String code() {
        return "Feishu";
    }

    @Override
    public Object configTemplate() {
        return new Config();
    }

    @Override
    public void push(JsonObject config, String content) {
        String url = str(config, "webhook");
        if (StringUtils.isBlank(url)) throw new EruptWebApiRuntimeException("Feishu webhook not configured");
        JsonObject body = new JsonObject();
        String secret = str(config, "webhookSecret");
        if (StringUtils.isNotBlank(secret)) {
            long timestamp = System.currentTimeMillis() / 1000;
            // Feishu quirk: timestamp + "\n" + secret is the HMAC key, the data is empty
            body.addProperty("timestamp", String.valueOf(timestamp));
            body.addProperty("sign", Base64.getEncoder().encodeToString(hmacSha256(timestamp + "\n" + secret, "")));
        }
        body.addProperty("msg_type", "text");
        body.add("content", textContent(content));
        this.post(url, body.toString());
    }

    @Override
    public String onCallback(JsonObject config, ChannelRequest request, Consumer<ChannelMessage> listener) {
        JsonObject body = GsonFactory.getGson().fromJson(request.getBody(), JsonObject.class);
        if (body.has("encrypt")) {
            body = GsonFactory.getGson().fromJson(decrypt(str(config, "encryptKey"), str(body, "encrypt")), JsonObject.class);
        }
        if ("url_verification".equals(str(body, "type"))) {
            JsonObject challenge = new JsonObject();
            challenge.addProperty("challenge", str(body, "challenge"));
            return challenge.toString();
        }
        JsonObject header = body.getAsJsonObject("header");
        if (null != header && "im.message.receive_v1".equals(str(header, "event_type"))) {
            JsonObject event = body.getAsJsonObject("event");
            JsonObject message = event.getAsJsonObject("message");
            if ("text".equals(str(message, "message_type"))) {
                JsonObject content = GsonFactory.getGson().fromJson(str(message, "content"), JsonObject.class);
                listener.accept(ChannelMessage.builder()
                        .content(str(content, "text").replaceAll("@_user_\\d+", "").trim())
                        .sender(str(event.getAsJsonObject("sender").getAsJsonObject("sender_id"), "open_id"))
                        .replyTo(str(message, "chat_id"))
                        .build());
            }
        }
        return "{}";
    }

    @Override
    public boolean testConnect(JsonObject config) {
        if (StringUtils.isBlank(str(config, "appId"))) return false;
        this.tenantToken(config);
        return true;
    }

    @Override
    public void reply(JsonObject config, ChannelMessage message, String content) {
        JsonObject body = new JsonObject();
        body.addProperty("receive_id", message.getReplyTo());
        body.addProperty("msg_type", "text");
        body.addProperty("content", textContent(content).toString());
        this.post(MESSAGE_API, body.toString(),
                Map.of("Authorization", "Bearer " + this.tenantToken(config)));
    }

    private String tenantToken(JsonObject config) {
        JsonObject auth = new JsonObject();
        auth.addProperty("app_id", str(config, "appId"));
        auth.addProperty("app_secret", str(config, "appSecret"));
        JsonObject token = GsonFactory.getGson().fromJson(this.post(TENANT_TOKEN_API, auth.toString()), JsonObject.class);
        String value = str(token, "tenant_access_token");
        if (null == value) throw new EruptWebApiRuntimeException("Feishu get token failed: " + token);
        return value;
    }

    private JsonObject textContent(String text) {
        JsonObject content = new JsonObject();
        content.addProperty("text", text);
        return content;
    }

    // AES-256-CBC with key = SHA-256(encryptKey), IV = the first 16 bytes of the payload
    @SneakyThrows
    private String decrypt(String encryptKey, String encrypt) {
        if (StringUtils.isBlank(encryptKey)) throw new EruptWebApiRuntimeException("Feishu encryptKey not configured");
        byte[] key = MessageDigest.getInstance("SHA-256").digest(encryptKey.getBytes(StandardCharsets.UTF_8));
        byte[] data = Base64.getDecoder().decode(encrypt);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(data, 0, 16));
        return new String(cipher.doFinal(data, 16, data.length - 16), StandardCharsets.UTF_8);
    }

    @Getter
    @Setter
    public static class Config {
        // Custom bot webhook for outbound push
        private String webhook = "";
        // Custom bot signing secret (optional)
        private String webhookSecret = "";
        // Self-built app credentials, used to reply to events
        private String appId = "";
        private String appSecret = "";
        // Event subscription encrypt key (optional; required if encryption is enabled)
        private String encryptKey = "";
    }

}
