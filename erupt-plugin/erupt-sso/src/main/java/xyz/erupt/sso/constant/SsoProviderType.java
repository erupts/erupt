package xyz.erupt.sso.constant;

import lombok.Getter;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.i18n.I18nTranslate;

import java.util.Arrays;
import java.util.List;

/**
 * Well known identity providers: the settings they are reached with and the wire protocol
 * they speak.
 *
 * <p>Picking one in the form pre-fills the endpoint, scope and claim fields, so an admin
 * only has to paste the client credentials and, for a self-hosted provider, replace the
 * {@code <placeholder>} parts of the URLs. The type is also consulted at login time: most
 * providers speak plain OAuth2 (a form encoded token POST answered in JSON, a bearer GET on
 * the user info endpoint), while DingTalk, WeCom and WeChat each improvise their own
 * exchange, which {@code EruptSsoService} implements per {@link Flow}.
 *
 * @author YuePeng
 * date 2026-09-24
 */
public enum SsoProviderType {

    CUSTOM(null),

    KEYCLOAK(Preset.oidc("Keycloak", "fa-brands fa-openid", "https://<host>/realms/<realm>")
            .scopes("openid profile email")
            .claims("preferred_username", "name", "email", "phone_number", "picture")),

    AUTHING(Preset.oidc("Authing", "fa-brands fa-openid", "https://<app>.authing.cn/oidc")
            .scopes("openid profile email phone")
            .claims("preferred_username", "name", "email", "phone_number", "picture")),

    CASDOOR(Preset.oidc("Casdoor", "fa-brands fa-openid", "https://<host>")
            .scopes("openid profile email phone")
            .claims("preferred_username", "displayName", "email", "phone", "picture")),

    OKTA(Preset.oidc("Okta", "fa-brands fa-openid", "https://<org>.okta.com/oauth2/default")
            .scopes("openid profile email")
            .claims("preferred_username", "name", "email", null, null)),

    AUTH0(Preset.oidc("Auth0", "fa-brands fa-openid", "https://<tenant>.auth0.com")
            .scopes("openid profile email")
            .claims("nickname", "name", "email", null, "picture")),

    MICROSOFT_ENTRA(Preset.oidc("Microsoft Entra ID", "fa-brands fa-microsoft", "https://login.microsoftonline.com/<tenant-id>/v2.0")
            .scopes("openid profile email")
            .claims("email", "name", "email", null, null)
            .credentials("Application (client) ID", "Client secret value")),

    GOOGLE(Preset.oidc("Google", "fa-brands fa-google", "https://accounts.google.com")
            .scopes("openid profile email")
            .claims("email", "name", "email", null, "picture")),

    GITLAB(Preset.oidc("GitLab", "fa-brands fa-gitlab", "https://gitlab.com")
            .scopes("openid profile email")
            .claims("preferred_username", "name", "email", null, "picture")
            .credentials("Application ID", "Secret")),

    ATLASSIAN(Preset.oidc("Atlassian", "fa-brands fa-atlassian", "https://auth.atlassian.com")
            .scopes("openid profile email")
            .claims("email", "name", "email", null, "picture")
            .openId("sub")
            .credentials("Client ID", "Secret")),

    SLACK(Preset.oidc("Slack", "fa-brands fa-slack", "https://slack.com")
            .scopes("openid profile email")
            // Slack namespaces its own claims by URL; the user id is what a bot message is addressed to
            .claims("email", "name", "email", null, "picture")
            .openId("https://slack.com/user_id")),

    ZOOM(Preset.oauth2("Zoom", "fa-brands fa-zoom",
                    "https://zoom.us/oauth/authorize",
                    "https://zoom.us/oauth/token",
                    "https://api.zoom.us/v2/users/me")
            .basicAuth()
            .scopes("user:read:user")
            .claims("email", "display_name", "email", null, "pic_url")
            .openId("id")),

    GITHUB(Preset.oauth2("GitHub", "fa-brands fa-github",
                    "https://github.com/login/oauth/authorize",
                    "https://github.com/login/oauth/access_token",
                    "https://api.github.com/user")
            .scopes("read:user user:email")
            .claims("login", "name", "email", null, "avatar_url")),

    GITEE(Preset.oauth2("Gitee", "fa-brands fa-gitee",
                    "https://gitee.com/oauth/authorize",
                    "https://gitee.com/oauth/token",
                    "https://gitee.com/api/v5/user")
            .scopes("user_info emails")
            .claims("login", "name", "email", null, "avatar_url")),

    FEISHU(Preset.oauth2("Feishu", "fa-solid fa-feather-pointed",
                    "https://accounts.feishu.cn/open-apis/authen/v1/authorize",
                    "https://open.feishu.cn/open-apis/authen/v2/oauth/token",
                    "https://open.feishu.cn/open-apis/authen/v1/user_info")
            .scopes("contact:user.base:readonly contact:user.email:readonly contact:user.phone:readonly contact:user.employee_id:readonly")
            .claims("user_id", "name", "email", "mobile", "avatar_url")
            .openId("open_id")
            .credentials("App ID", "App Secret")),

    DINGTALK(Preset.flow(Flow.DINGTALK, "DingTalk", "fa-solid fa-comment-dots",
                    "https://login.dingtalk.com/oauth2/auth",
                    "https://api.dingtalk.com/v1.0/oauth2/userAccessToken",
                    "https://api.dingtalk.com/v1.0/contact/users/me")
            .scopes("openid")
            .claims("mobile", "nick", "email", "mobile", "avatarUrl")
            // the app scoped openId cannot be messaged; the unionId is what the corp userid is looked up by
            .openId("unionId")
            .credentials("AppKey (Client ID)", "AppSecret (Client Secret)")),

    WECOM(Preset.flow(Flow.WECOM, "WeCom", "fa-brands fa-weixin",
                    "https://login.work.weixin.qq.com/wwlogin/sso/login?login_type=CorpApp&agentid=<agentid>",
                    "https://qyapi.weixin.qq.com/cgi-bin/gettoken",
                    "https://qyapi.weixin.qq.com/cgi-bin/user/get")
            .scopes("snsapi_privateinfo")
            .claims("userid", "name", "email", "mobile", "avatar")
            .openId("userid")
            .credentials("CorpID", "Secret of the self-built app")),

    WECHAT(Preset.flow(Flow.WECHAT, "WeChat", "fa-brands fa-weixin",
                    "https://open.weixin.qq.com/connect/qrconnect",
                    "https://api.weixin.qq.com/sns/oauth2/access_token",
                    "https://api.weixin.qq.com/sns/userinfo")
            .scopes("snsapi_login")
            .claims("openid", "nickname", "email", null, "headimgurl")
            .openId("openid")
            .credentials("AppID", "AppSecret"));

    private final Preset preset;

    SsoProviderType(Preset preset) {
        this.preset = preset;
    }

    /**
     * The settings to pre-fill, or {@code null} for {@link #CUSTOM}, which leaves the form alone.
     */
    public Preset preset() {
        return preset;
    }

    /**
     * How the provider is talked to at login. A custom row speaks plain OAuth2.
     */
    public Flow flow() {
        return null == preset ? Flow.OAUTH2 : preset.getFlow();
    }

    /**
     * The exchange a provider expects once the browser comes back with a code.
     */
    public enum Flow {
        /**
         * RFC 6749 authorization code: form encoded token POST, bearer GET on user info.
         * OIDC providers are the same flow with an issuer to discover the endpoints from.
         */
        OAUTH2,
        /**
         * DingTalk unified login: a JSON token POST, the token then travels in a custom header.
         */
        DINGTALK,
        /**
         * WeCom: the code is resolved with the corp's own access token, not a user token,
         * and the profile is read from the contact book in a second call.
         */
        WECOM,
        /**
         * WeChat open platform: token and user info are both GETs with query parameters,
         * and the user info call needs the openid the token call returned.
         */
        WECHAT
    }

    /**
     * Everything a provider preset knows how to fill in. An OIDC provider carries an issuer and
     * lets discovery find the endpoints; every other one spells the three endpoints out.
     */
    @Getter
    public static class Preset {

        private final Flow flow;

        private final String name;

        private final String icon;

        private String issuer;

        private String authorizeUrl;

        private String tokenUrl;

        private String userInfoUrl;

        private String scopes;

        // the token endpoint insists on HTTP Basic client authentication instead of body parameters
        private boolean basicAuth;

        private String accountClaim;

        private String nameClaim;

        private String emailClaim;

        private String phoneClaim;

        private String avatarClaim;

        private String openIdClaim;

        // what the provider's console calls the two credentials, shown under the fields
        private String clientIdHint;

        private String clientSecretHint;

        private Preset(Flow flow, String name, String icon) {
            this.flow = flow;
            this.name = name;
            this.icon = icon;
        }

        static Preset oidc(String name, String icon, String issuer) {
            Preset preset = new Preset(Flow.OAUTH2, name, icon);
            preset.issuer = issuer;
            return preset;
        }

        static Preset oauth2(String name, String icon, String authorizeUrl, String tokenUrl, String userInfoUrl) {
            return flow(Flow.OAUTH2, name, icon, authorizeUrl, tokenUrl, userInfoUrl);
        }

        static Preset flow(Flow flow, String name, String icon, String authorizeUrl, String tokenUrl, String userInfoUrl) {
            Preset preset = new Preset(flow, name, icon);
            preset.authorizeUrl = authorizeUrl;
            preset.tokenUrl = tokenUrl;
            preset.userInfoUrl = userInfoUrl;
            return preset;
        }

        Preset basicAuth() {
            this.basicAuth = true;
            return this;
        }

        Preset scopes(String scopes) {
            this.scopes = scopes;
            return this;
        }

        Preset claims(String account, String name, String email, String phone, String avatar) {
            this.accountClaim = account;
            this.nameClaim = name;
            this.emailClaim = email;
            this.phoneClaim = phone;
            this.avatarClaim = avatar;
            return this;
        }

        Preset openId(String openIdClaim) {
            this.openIdClaim = openIdClaim;
            return this;
        }

        Preset credentials(String clientIdHint, String clientSecretHint) {
            this.clientIdHint = clientIdHint;
            this.clientSecretHint = clientSecretHint;
            return this;
        }

    }

    /**
     * Dropdown source: the constant name is the stored value, the preset name is the label.
     * {@code CUSTOM} has no preset, so its label goes through translation instead.
     */
    public static class H implements ChoiceFetchHandler<Void> {

        @Override
        public List<VLModel> fetch(String[] params) {
            return Arrays.stream(SsoProviderType.values()).map(type -> new VLModel(type.name(),
                    null == type.preset ? I18nTranslate.$translate("Custom") : type.preset.getName())).toList();
        }

    }

}
