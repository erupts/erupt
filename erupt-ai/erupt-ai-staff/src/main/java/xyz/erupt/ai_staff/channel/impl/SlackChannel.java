package xyz.erupt.ai_staff.channel.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_staff.channel.ChannelMessage;
import xyz.erupt.ai_staff.channel.ChannelRequest;
import xyz.erupt.ai_staff.channel.StaffChannel;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Slack: push via incoming webhook, inbound via the Events API
 * (app_mention in channels, message in DMs) — replies via chat.postMessage.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Slf4j
@Component
public class SlackChannel extends StaffChannel {

    private static final String POST_MESSAGE_API = "https://slack.com/api/chat.postMessage";

    @Override
    public String code() {
        return "Slack";
    }

    @Override
    public Object configTemplate() {
        return new Config();
    }

    @Override
    public void push(JsonObject config, String content) {
        String url = str(config, "webhook");
        if (StringUtils.isBlank(url)) throw new EruptWebApiRuntimeException("Slack webhook not configured");
        JsonObject body = new JsonObject();
        body.addProperty("text", content);
        this.post(url, body.toString());
    }

    @Override
    public String onCallback(JsonObject config, ChannelRequest request, Consumer<ChannelMessage> listener) {
        String signingSecret = str(config, "signingSecret");
        if (StringUtils.isNotBlank(signingSecret)) {
            String timestamp = request.header("x-slack-request-timestamp");
            String expected = "v0=" + hex(hmacSha256(signingSecret, "v0:" + timestamp + ":" + request.getBody()));
            if (!expected.equals(request.header("x-slack-signature"))) {
                throw new EruptWebApiRuntimeException("Slack signature mismatch");
            }
        }
        JsonObject body = GsonFactory.getGson().fromJson(request.getBody(), JsonObject.class);
        if ("url_verification".equals(str(body, "type"))) {
            JsonObject challenge = new JsonObject();
            challenge.addProperty("challenge", str(body, "challenge"));
            return challenge.toString();
        }
        // Slack redelivers on slow acks; process first delivery only
        if (null != request.header("x-slack-retry-num")) return "";
        if ("event_callback".equals(str(body, "type"))) {
            JsonObject event = body.getAsJsonObject("event");
            String type = str(event, "type");
            // Skip bot echoes and message edits; plain "message" events only for DMs
            // (channel mentions already arrive as app_mention)
            if (event.has("bot_id") || event.has("subtype")) return "";
            boolean mention = "app_mention".equals(type);
            boolean directMessage = "message".equals(type) && "im".equals(str(event, "channel_type"));
            if (mention || directMessage) {
                listener.accept(ChannelMessage.builder()
                        .content(str(event, "text").replaceAll("<@[A-Z0-9]+>", "").trim())
                        .sender(str(event, "user"))
                        .replyTo(str(event, "channel"))
                        .build());
            }
        }
        return "";
    }

    @Override
    public void reply(JsonObject config, ChannelMessage message, String content) {
        JsonObject body = new JsonObject();
        body.addProperty("channel", message.getReplyTo());
        body.addProperty("text", content);
        this.post(POST_MESSAGE_API, body.toString(),
                Map.of("Authorization", "Bearer " + str(config, "botToken")));
    }

    @Getter
    @Setter
    public static class Config {
        // Incoming webhook for outbound push
        private String webhook = "";
        // Bot token (xoxb-...) used to reply to events
        private String botToken = "";
        // Signing secret used to verify inbound callbacks
        private String signingSecret = "";
    }

}
