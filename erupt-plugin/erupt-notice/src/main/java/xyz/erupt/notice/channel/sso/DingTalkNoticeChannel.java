package xyz.erupt.notice.channel.sso;

import com.google.gson.JsonObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.sso.constant.SsoProviderType;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.service.SsoProviderApi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DingTalk work notification. Login only tells erupt the user's unionId; the corp userid
 * a work notification is addressed to is looked up from it once and remembered.
 */
@Component
@ConditionalOnClass(name = "xyz.erupt.sso.service.EruptSsoService")
public class DingTalkNoticeChannel extends AbstractSsoNoticeChannel {

    private final Map<String, String> userIdByUnionId = new ConcurrentHashMap<>();

    @Override
    protected SsoProviderType type() {
        return SsoProviderType.DINGTALK;
    }

    @Override
    protected void deliver(EruptSso sso, String unionId, NoticeMessage message) {
        String token = api.appAccessToken(sso);
        String legacy = SsoProviderApi.dingTalkLegacyBase(sso);
        String userId = userIdByUnionId.computeIfAbsent(unionId, id -> this.userId(legacy, token, id));

        StringBuilder text = new StringBuilder("### ").append(message.getTitle()).append("\n\n").append(message.getContent());
        String link = link(message);
        if (null != link) text.append("\n\n[").append(viewDetails()).append("](").append(link).append(")");
        JsonObject markdown = new JsonObject();
        markdown.addProperty("title", message.getTitle());
        markdown.addProperty("text", text.toString());
        JsonObject msg = new JsonObject();
        msg.addProperty("msgtype", "markdown");
        msg.add("markdown", markdown);
        JsonObject body = new JsonObject();
        body.addProperty("agent_id", messagingKey(sso));
        body.addProperty("userid_list", userId);
        body.add("msg", msg);
        api.postJson(legacy + "topapi/message/corpconversation/asyncsend_v2?access_token=" + SsoProviderApi.urlEncode(token),
                Map.of(), body, "dingtalk message");
    }

    private String userId(String legacy, String token, String unionId) {
        JsonObject body = new JsonObject();
        body.addProperty("unionid", unionId);
        JsonObject json = api.postJson(legacy + "topapi/user/getbyunionid?access_token=" + SsoProviderApi.urlEncode(token),
                Map.of(), body, "dingtalk user");
        JsonObject result = json.has("result") && json.get("result").isJsonObject() ? json.getAsJsonObject("result") : null;
        String userId = SsoProviderApi.claim(result, "userid");
        if (null == userId) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("notice.sso_not_bound") + " (unionId " + unionId + ")");
        return userId;
    }

}
