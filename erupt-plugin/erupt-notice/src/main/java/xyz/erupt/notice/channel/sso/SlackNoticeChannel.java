package xyz.erupt.notice.channel.sso;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.sso.constant.SsoProviderType;
import xyz.erupt.sso.model.EruptSso;

import java.util.Map;

/**
 * Slack direct message from the app's bot user, so the row's messaging key has to be the
 * bot token the Slack console issued on installation; the OIDC client secret cannot post.
 */
@Component
@ConditionalOnClass(name = "xyz.erupt.sso.service.EruptSsoService")
public class SlackNoticeChannel extends AbstractSsoNoticeChannel {

    @Override
    protected SsoProviderType type() {
        return SsoProviderType.SLACK;
    }

    @Override
    protected void deliver(EruptSso sso, String userId, NoticeMessage message) {
        String botToken = messagingKey(sso);
        StringBuilder text = new StringBuilder("*").append(message.getTitle()).append("*\n").append(message.getContent());
        String link = link(message);
        if (null != link) text.append("\n<").append(link).append("|").append(viewDetails()).append(">");
        JsonObject body = new JsonObject();
        body.addProperty("channel", userId);
        body.addProperty("text", text.toString());
        String base = StringUtils.removeEnd(StringUtils.defaultIfBlank(sso.getIssuer(), "https://slack.com"), "/");
        api.postJson(base + "/api/chat.postMessage", Map.of("Authorization", "Bearer " + botToken), body, "slack message");
    }

}
