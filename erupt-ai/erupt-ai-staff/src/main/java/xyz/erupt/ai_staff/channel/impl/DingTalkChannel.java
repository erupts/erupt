package xyz.erupt.ai_staff.channel.impl;

import com.google.gson.JsonArray;
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

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.function.Consumer;

/**
 * DingTalk enterprise (internal) robot: the developer console only exposes a
 * Client ID (AppKey) and Client Secret (AppSecret). Outbound push exchanges them
 * for an access token and calls the robot send API (1:1 by userIds or a group by
 * openConversationId); inbound callbacks are verified with the Client Secret and
 * answered via the sessionWebhook the callback carries.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Slf4j
@Component
public class DingTalkChannel extends StaffChannel {

    private static final String ACCESS_TOKEN_API = "https://api.dingtalk.com/v1.0/oauth2/accessToken";

    private static final String OTO_SEND_API = "https://api.dingtalk.com/v1.0/robot/oToMessages/batchSend";

    private static final String GROUP_SEND_API = "https://api.dingtalk.com/v1.0/robot/groupMessages/send";

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
        String token = accessToken(config);
        String robotCode = str(config, "clientId");
        String conversationId = str(config, "openConversationId");
        JsonObject body = new JsonObject();
        body.addProperty("robotCode", robotCode);
        body.addProperty("msgKey", "sampleMarkdown");
        body.addProperty("msgParam", markdownParam(content));
        Map<String, String> headers = Map.of("x-acs-dingtalk-access-token", token);
        if (StringUtils.isNotBlank(conversationId)) {
            body.addProperty("openConversationId", conversationId);
            this.post(GROUP_SEND_API, body.toString(), headers);
            return;
        }
        String userIds = str(config, "userIds");
        if (StringUtils.isNotBlank(userIds)) {
            JsonArray ids = new JsonArray();
            Arrays.stream(userIds.split(",")).map(String::trim).filter(StringUtils::isNotBlank).forEach(ids::add);
            body.add("userIds", ids);
            this.post(OTO_SEND_API, body.toString(), headers);
            return;
        }
        throw new EruptWebApiRuntimeException("DingTalk push target not configured (userIds or openConversationId)");
    }

    @Override
    public boolean testConnect(JsonObject config) {
        if (StringUtils.isBlank(str(config, "clientId"))) return false;
        // Exchanging the credentials for an access token validates clientId/clientSecret
        accessToken(config);
        return true;
    }

    // Exchange Client ID / Client Secret for a short-lived access token
    private String accessToken(JsonObject config) {
        String clientId = str(config, "clientId");
        String clientSecret = str(config, "clientSecret");
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
            throw new EruptWebApiRuntimeException("DingTalk clientId/clientSecret not configured");
        }
        JsonObject auth = new JsonObject();
        auth.addProperty("appKey", clientId);
        auth.addProperty("appSecret", clientSecret);
        JsonObject token = GsonFactory.getGson().fromJson(this.post(ACCESS_TOKEN_API, auth.toString()), JsonObject.class);
        String value = str(token, "accessToken");
        if (null == value) throw new EruptWebApiRuntimeException("DingTalk get token failed: " + token);
        return value;
    }

    @Override
    public String onCallback(JsonObject config, ChannelRequest request, Consumer<ChannelMessage> listener) {
        String clientSecret = str(config, "clientSecret");
        if (StringUtils.isNotBlank(clientSecret)) {
            String timestamp = request.header("timestamp");
            String expected = Base64.getEncoder().encodeToString(hmacSha256(clientSecret, timestamp + "\n" + clientSecret));
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
        // sessionWebhook is a pre-authorized webhook, no access token needed
        this.post(message.getReplyTo(), markdownBody(content));
    }

    // Webhook message body (sessionWebhook reply)
    private String markdownBody(String content) {
        JsonObject markdown = new JsonObject();
        markdown.addProperty("title", "AI Staff");
        markdown.addProperty("text", content);
        JsonObject body = new JsonObject();
        body.addProperty("msgtype", "markdown");
        body.add("markdown", markdown);
        return body.toString();
    }

    // Robot send API msgParam (a JSON string) for msgKey = sampleMarkdown
    private String markdownParam(String content) {
        JsonObject param = new JsonObject();
        param.addProperty("title", "AI Staff");
        param.addProperty("text", content);
        return param.toString();
    }

    @Getter
    @Setter
    public static class Config {
        // Client ID (AppKey) from the DingTalk developer console; also used as robotCode
        private String clientId = "";
        // Client Secret (AppSecret): token exchange + inbound callback signature
        private String clientSecret = "";
        // 1:1 push targets — DingTalk userIds, comma-separated (used when openConversationId is blank)
        private String userIds = "";
        // Group push target — scene group openConversationId (takes precedence over userIds)
        private String openConversationId = "";
    }

}
