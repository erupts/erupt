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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;

/**
 * DingTalk: push via group robot webhook (optional signed), inbound via the
 * enterprise robot HTTP callback — replies go to the sessionWebhook it carries.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Slf4j
@Component
public class DingTalkChannel extends StaffChannel {

    @Override
    public String code() {
        return "DingTalk";
    }

    @Override
    public Object configTemplate() {
        return new Config();
    }

    @Override
    public void push(JsonObject config, String content) {
        this.post(signedUrl(config), markdownBody(content));
    }

    @Override
    public boolean testConnect(JsonObject config) {
        if (StringUtils.isBlank(str(config, "webhook"))) return false;
        // Post an empty-content text message: DingTalk rejects it before delivery
        // (nothing visible is sent), yet it still passes the signature stage — enough
        // to verify the webhook is reachable and, when a secret is set, that the sign matches.
        JsonObject text = new JsonObject();
        text.addProperty("content", "");
        JsonObject body = new JsonObject();
        body.addProperty("msgtype", "text");
        body.add("text", text);
        JsonObject result = GsonFactory.getGson().fromJson(this.post(signedUrl(config), body.toString()), JsonObject.class);
        int errcode = result.has("errcode") ? result.get("errcode").getAsInt() : -1;
        // 310000 with a secret configured means the signature did not match
        if (StringUtils.isNotBlank(str(config, "secret")) && errcode == 310000) {
            throw new EruptWebApiRuntimeException("DingTalk sign mismatch: " + str(result, "errmsg"));
        }
        return true;
    }

    @SneakyThrows
    private String signedUrl(JsonObject config) {
        String url = str(config, "webhook");
        if (StringUtils.isBlank(url)) throw new EruptWebApiRuntimeException("DingTalk webhook not configured");
        String secret = str(config, "secret");
        if (StringUtils.isNotBlank(secret)) {
            long timestamp = System.currentTimeMillis();
            String sign = URLEncoder.encode(Base64.getEncoder()
                    .encodeToString(hmacSha256(secret, timestamp + "\n" + secret)), StandardCharsets.UTF_8);
            url += "&timestamp=" + timestamp + "&sign=" + sign;
        }
        return url;
    }

    @Override
    public String onCallback(JsonObject config, ChannelRequest request, Consumer<ChannelMessage> listener) {
        String appSecret = str(config, "appSecret");
        if (StringUtils.isNotBlank(appSecret)) {
            String timestamp = request.header("timestamp");
            String expected = Base64.getEncoder().encodeToString(hmacSha256(appSecret, timestamp + "\n" + appSecret));
            if (!expected.equals(request.header("sign"))) {
                throw new EruptWebApiRuntimeException("DingTalk sign mismatch");
            }
        }
        JsonObject body = GsonFactory.getGson().fromJson(request.getBody(), JsonObject.class);
        if (body.has("text")) {
            listener.accept(ChannelMessage.builder()
                    .content(body.getAsJsonObject("text").get("content").getAsString().trim())
                    .sender(str(body, "senderStaffId"))
                    .senderName(str(body, "senderNick"))
                    .replyTo(str(body, "sessionWebhook"))
                    .build());
        }
        return "{}";
    }

    @Override
    public void reply(JsonObject config, ChannelMessage message, String content) {
        this.post(message.getReplyTo(), markdownBody(content));
    }

    private String markdownBody(String content) {
        JsonObject markdown = new JsonObject();
        markdown.addProperty("title", "AI Staff");
        markdown.addProperty("text", content);
        JsonObject body = new JsonObject();
        body.addProperty("msgtype", "markdown");
        body.add("markdown", markdown);
        return body.toString();
    }

    @Getter
    @Setter
    public static class Config {
        // Group robot webhook for outbound push
        private String webhook = "";
        // Group robot signing secret (optional)
        private String secret = "";
        // Enterprise robot appSecret, used to verify inbound callbacks
        private String appSecret = "";
    }

}
