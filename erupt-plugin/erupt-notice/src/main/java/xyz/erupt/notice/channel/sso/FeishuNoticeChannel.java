package xyz.erupt.notice.channel.sso;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.sso.constant.SsoProviderType;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.service.SsoProviderApi;

import java.util.Map;

/**
 * Feishu bot message to the user's open_id, as a rich text post: the title on top, the
 * content below and, when the message carries an absolute URL, a link line.
 */
@Component
@ConditionalOnClass(name = "xyz.erupt.sso.service.EruptSsoService")
public class FeishuNoticeChannel extends AbstractSsoNoticeChannel {

    @Override
    protected SsoProviderType type() {
        return SsoProviderType.FEISHU;
    }

    @Override
    protected void deliver(EruptSso sso, String openId, NoticeMessage message) {
        JsonArray lines = new JsonArray();
        lines.add(line("text", message.getContent(), null));
        String link = link(message);
        if (null != link) lines.add(line("a", viewDetails(), link));
        JsonObject post = new JsonObject();
        post.addProperty("title", message.getTitle());
        post.add("content", lines);
        JsonObject content = new JsonObject();
        content.add("zh_cn", post);

        JsonObject body = new JsonObject();
        body.addProperty("receive_id", openId);
        body.addProperty("msg_type", "post");
        // the content is a JSON document carried as a string, that is the API's own shape
        body.addProperty("content", content.toString());
        api.postJson(SsoProviderApi.feishuBase(sso) + "im/v1/messages?receive_id_type=open_id",
                Map.of("Authorization", "Bearer " + api.appAccessToken(sso)), body, "feishu message");
    }

    private static JsonArray line(String tag, String text, String href) {
        JsonObject element = new JsonObject();
        element.addProperty("tag", tag);
        element.addProperty("text", null == text ? "" : text);
        if (null != href) element.addProperty("href", href);
        JsonArray line = new JsonArray();
        line.add(element);
        return line;
    }

}
