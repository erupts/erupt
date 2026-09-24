package xyz.erupt.notice.channel.sso;

import com.google.gson.JsonObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.sso.constant.SsoProviderType;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.service.SsoProviderApi;

import java.util.Map;

/**
 * WeCom application message to the member's userid: a text card when there is a link to
 * open, otherwise plain text, since a card without a URL is refused by the API.
 */
@Component
@ConditionalOnClass(name = "xyz.erupt.sso.service.EruptSsoService")
public class WeComNoticeChannel extends AbstractSsoNoticeChannel {

    @Override
    protected SsoProviderType type() {
        return SsoProviderType.WECOM;
    }

    @Override
    protected void deliver(EruptSso sso, String userId, NoticeMessage message) {
        JsonObject body = new JsonObject();
        body.addProperty("touser", userId);
        body.addProperty("agentid", messagingKey(sso));
        String link = link(message);
        if (null != link) {
            JsonObject card = new JsonObject();
            card.addProperty("title", message.getTitle());
            card.addProperty("description", message.getContent());
            card.addProperty("url", link);
            card.addProperty("btntxt", viewDetails());
            body.addProperty("msgtype", "textcard");
            body.add("textcard", card);
        } else {
            JsonObject text = new JsonObject();
            text.addProperty("content", message.getTitle() + "\n" + message.getContent());
            body.addProperty("msgtype", "text");
            body.add("text", text);
        }
        api.postJson(SsoProviderApi.cgiBase(sso.getUserInfoUrl()) + "message/send?access_token=" + SsoProviderApi.urlEncode(api.appAccessToken(sso)),
                Map.of(), body, "wecom message");
    }

}
