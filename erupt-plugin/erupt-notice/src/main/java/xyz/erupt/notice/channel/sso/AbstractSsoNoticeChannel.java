package xyz.erupt.notice.channel.sso;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.notice.channel.AbstractNoticeChannel;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.sso.constant.SsoProviderType;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.model.EruptSsoBind;
import xyz.erupt.sso.service.EruptSsoBindService;
import xyz.erupt.sso.service.EruptSsoService;
import xyz.erupt.sso.service.SsoProviderApi;
import xyz.erupt.upms.model.EruptUser;

/**
 * A notification channel that pushes through an SSO provider row: the row's credentials
 * are the app the message is sent from, and the recipient is the identifier the row mapped
 * onto the user's binding at their last login. Only offered while an enabled row of the
 * provider type exists, and only able to reach users who have signed in through it.
 *
 * <p>A relative URL on the message is meant for the admin UI and means nothing inside a
 * chat app, so a link is only attached when the URL is absolute.
 *
 * @author YuePeng
 * date 2026-09-24
 */
public abstract class AbstractSsoNoticeChannel extends AbstractNoticeChannel {

    @Resource
    protected EruptSsoService eruptSsoService;

    @Resource
    protected EruptSsoBindService bindService;

    @Resource
    protected SsoProviderApi api;

    /**
     * The provider type this channel sends through.
     */
    protected abstract SsoProviderType type();

    /**
     * Push one message to one recipient, as identified at the provider.
     */
    protected abstract void deliver(EruptSso sso, String openId, NoticeMessage message);

    @Override
    public String name() {
        String brand = type().preset().getName();
        return eruptSsoService.findEnabled(type())
                .map(sso -> brand + " (" + I18nTranslate.$translate(sso.getName()) + ")")
                .orElse(brand);
    }

    @Override
    public boolean available() {
        return eruptSsoService.findEnabled(type()).isPresent();
    }

    @Override
    public void send(EruptUser receiveUser, NoticeMessage noticeMessage) {
        EruptSso sso = eruptSsoService.findEnabled(type()).orElseThrow(() ->
                new EruptWebApiRuntimeException(I18nTranslate.$translate("notice.sso_not_configured") + " (" + type() + ")"));
        String openId = bindService.find(receiveUser.getId(), sso.getCode())
                .map(EruptSsoBind::getOpenId).filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new EruptWebApiRuntimeException(I18nTranslate.$translate("notice.sso_not_bound")
                        + " (" + receiveUser.getAccount() + " @ " + sso.getCode() + ")"));
        this.deliver(sso, openId, noticeMessage);
    }

    /**
     * The provider row's messaging key, whatever the provider calls it: an agent id for
     * WeCom and DingTalk, a bot token for Slack. Missing means the row was set up for login only.
     */
    protected static String messagingKey(EruptSso sso) {
        if (StringUtils.isBlank(sso.getMessagingKey())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("notice.sso_key_missing") + " (" + sso.getCode() + ")");
        }
        return sso.getMessagingKey();
    }

    protected static String link(NoticeMessage message) {
        String url = message.getUrl();
        return null != url && (url.startsWith("http://") || url.startsWith("https://")) ? url : null;
    }

    protected static String viewDetails() {
        return I18nTranslate.$translate("notice.view_details");
    }

}
