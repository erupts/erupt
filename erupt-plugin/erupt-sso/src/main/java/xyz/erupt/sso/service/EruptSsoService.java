package xyz.erupt.sso.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.EncryptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.constant.EncryptType;
import xyz.erupt.sso.constant.SsoProviderType;
import xyz.erupt.sso.constant.SsoSessionKey;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.model.EruptSsoBind;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.sso.vo.EruptSsoProviderVo;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Single sign-on over the OAuth2 authorization code flow, with PKCE.
 *
 * <p>erupt is the relying party and never the token audience: the code is exchanged server
 * side and the resulting access token is spent on one call, the provider's user info
 * endpoint. Nothing the browser sends is trusted — the state carries no payload of its own,
 * it is a key into a short lived server side record holding the provider and the PKCE
 * verifier, and it is burnt on first use.
 *
 * @author YuePeng
 * date 2026-09-18
 */
@Service
public class EruptSsoService {

    private static final int STATE_EXPIRE_MINUTES = 10;

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    // the ticket only has to survive one browser redirect
    private static final int TICKET_EXPIRE_SECONDS = 60;


    // Providers disagree on what the stable identifier is called; OIDC says sub, the rest improvise
    private static final String[] SUBJECT_CLAIMS = {"sub", "id", "openid", "open_id", "openId", "unionid", "union_id", "unionId", "userid", "userId", "user_id"};

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptSessionService sessionService;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptAppProp eruptAppProp;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private SsoProviderApi api;

    // A discovery document is a deployment constant; cache it per issuer and drop it when the row changes
    private final Map<String, Endpoints> discoveryCache = new ConcurrentHashMap<>();

    /**
     * Providers offered on the login page. Nothing secret leaves this method.
     */
    public List<EruptSsoProviderVo> providers() {
        return eruptDao.lambdaQuery(EruptSso.class).eq(EruptSso::getStatus, true)
                .orderBy(EruptSso::getSort).list().stream()
                .map(it -> new EruptSsoProviderVo(it.getCode(), I18nTranslate.$translate(it.getName()), it.getIcon()))
                .collect(Collectors.toList());
    }

    public void evictDiscovery(String issuer) {
        if (StringUtils.isNotBlank(issuer)) discoveryCache.remove(issuer);
    }

    public void evictAppToken(Long ssoId) {
        api.evictAppToken(ssoId);
    }

    /**
     * The enabled provider row of a type, the first by sort when there are several; what a
     * notification channel for that provider sends through.
     */
    public Optional<EruptSso> findEnabled(SsoProviderType type) {
        return eruptDao.lambdaQuery(EruptSso.class).eq(EruptSso::getType, type).eq(EruptSso::getStatus, true)
                .orderBy(EruptSso::getSort).list().stream().findFirst();
    }

    // ------------------------------------------------------------- authorize

    /**
     * Where the browser has to go to meet the provider, with a freshly parked state.
     */
    public String authorizeUrl(String providerCode, HttpServletRequest request) {
        EruptSso sso = this.findEnabled(providerCode);
        Endpoints endpoints = this.endpoints(sso);
        String state = Erupts.generateCode(32);
        String verifier = Erupts.generateCode(64);
        SsoState payload = new SsoState();
        payload.setSsoId(sso.getId());
        payload.setVerifier(verifier);
        sessionService.put(SsoSessionKey.SSO_STATE + state, GsonFactory.getGson().toJson(payload), STATE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        Map<String, String> params = new LinkedHashMap<>();
        switch (flow(sso)) {
            case WECOM, WECHAT -> {
                // Tencent calls the client id appid; WeCom's agent id is part of the configured URL
                params.put("appid", sso.getClientId());
                params.put("redirect_uri", this.redirectUri(sso, request));
                params.put("response_type", "code");
                params.put("scope", sso.getScopes());
                params.put("state", state);
            }
            case DINGTALK -> {
                params.put("response_type", "code");
                params.put("client_id", sso.getClientId());
                params.put("redirect_uri", this.redirectUri(sso, request));
                params.put("scope", sso.getScopes());
                params.put("state", state);
                params.put("prompt", "consent");
            }
            default -> {
                params.put("response_type", "code");
                params.put("client_id", sso.getClientId());
                params.put("redirect_uri", this.redirectUri(sso, request));
                params.put("scope", sso.getScopes());
                params.put("state", state);
                params.put("code_challenge", challenge(verifier));
                params.put("code_challenge_method", "S256");
            }
        }
        return authorizeQuery(endpoints.authorizeUrl(), params);
    }

    private static SsoProviderType.Flow flow(EruptSso sso) {
        return null == sso.getType() ? SsoProviderType.Flow.OAUTH2 : sso.getType().flow();
    }

    // -------------------------------------------------------------- callback

    /**
     * Turn the provider's authorization code into an erupt session, handed back as a
     * one-time ticket: a session token has no business travelling in a redirect URL.
     */
    public String callback(String providerCode, String state, String authCode, HttpServletRequest request) {
        Erupts.requireTrue(StringUtils.isNotBlank(state) && StringUtils.isNotBlank(authCode),
                I18nTranslate.$translate("sso.state_invalid"));
        Object raw = sessionService.get(SsoSessionKey.SSO_STATE + state);
        sessionService.remove(SsoSessionKey.SSO_STATE + state); // single use, whatever happens next
        Erupts.requireNonNull(raw, I18nTranslate.$translate("sso.state_invalid"));
        SsoState payload = GsonFactory.getGson().fromJson(raw.toString(), SsoState.class);
        EruptSso sso = this.findEnabled(providerCode);
        // the state was issued for one provider; a code replayed against another must not pass
        Erupts.requireTrue(sso.getId().equals(payload.getSsoId()), I18nTranslate.$translate("sso.state_invalid"));

        Endpoints endpoints = this.endpoints(sso);
        JsonObject claims = switch (flow(sso)) {
            case DINGTALK -> this.dingTalkIdentity(sso, endpoints, authCode);
            case WECOM -> this.weComIdentity(sso, endpoints, authCode);
            case WECHAT -> this.weChatIdentity(sso, endpoints, authCode);
            default -> this.userInfo(endpoints, this.exchangeCode(sso, endpoints, authCode, payload.getVerifier(), request));
        };
        // only now is there anything to write, and the provider is no longer on the line
        EruptUser eruptUser = transactionTemplate.execute(status -> this.resolveUser(sso, claims));

        String reason = eruptUserService.checkAccountUsable(eruptUser);
        if (null != reason) throw new EruptWebApiRuntimeException(reason);

        LoginModel loginModel = new LoginModel(true, eruptUser);
        eruptUserService.completeLogin(loginModel, EruptUserService.findEruptLogin());
        String ticket = Erupts.generateCode(32);
        sessionService.put(SsoSessionKey.SSO_TICKET + ticket, GsonFactory.getGson().toJson(loginModel), TICKET_EXPIRE_SECONDS, TimeUnit.SECONDS);
        return ticket;
    }

    /**
     * Trade the redirect ticket for the session it stands for. Null once it is spent or stale.
     */
    public LoginModel consumeTicket(String ticket) {
        if (StringUtils.isBlank(ticket)) return null;
        Object raw = sessionService.get(SsoSessionKey.SSO_TICKET + ticket);
        if (null == raw) return null;
        sessionService.remove(SsoSessionKey.SSO_TICKET + ticket);
        return GsonFactory.getGson().fromJson(raw.toString(), LoginModel.class);
    }

    // ------------------------------------------------------------ user match

    /**
     * Who signed in. The subject owns the binding; a claim may only ever create one.
     */
    private EruptUser resolveUser(EruptSso sso, JsonObject claims) {
        String subject = this.subject(claims);
        Erupts.requireTrue(StringUtils.isNotBlank(subject), I18nTranslate.$translate("sso.no_subject") + claimNames(claims));
        EruptSsoBind bind = eruptDao.lambdaQuery(EruptSsoBind.class)
                .eq(EruptSsoBind::getSso, sso).eq(EruptSsoBind::getSubject, subject).one();
        if (null != bind) {
            this.recordLogin(sso, bind, claims);
            eruptDao.merge(bind);
            return this.applyProfile(sso, claims, bind.getEruptUser());
        }

        String account = claim(claims, sso.getAccountClaim());
        if (StringUtils.isBlank(account)) account = claim(claims, sso.getEmailClaim());
        Erupts.requireTrue(StringUtils.isNotBlank(account), I18nTranslate.$translate("sso.no_account_claim") + claimNames(claims));
        EruptUser eruptUser = eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, account).one();
        if (null == eruptUser) {
            // the value is the caller's own identifier at the provider; naming it tells the admin what to create
            Erupts.requireTrue(Boolean.TRUE.equals(sso.getAutoCreate()),
                    I18nTranslate.$translate("sso.account_not_found") + " (" + sso.getAccountClaim() + ": " + account + ")");
            eruptUser = this.createUser(sso, claims, account);
        }
        EruptSsoBind newBind = new EruptSsoBind();
        newBind.setSso(sso);
        newBind.setEruptUser(eruptUser);
        newBind.setSubject(subject);
        this.recordLogin(sso, newBind, claims);
        eruptDao.persist(newBind);
        return this.applyProfile(sso, claims, eruptUser);
    }

    /**
     * What the binding remembers about the provider side, rewritten on every login so the
     * identifiers and the snapshot other modules read are the ones the provider last vouched for.
     */
    private void recordLogin(EruptSso sso, EruptSsoBind bind, JsonObject claims) {
        bind.setOpenId(claim(claims, sso.getOpenIdClaim()));
        bind.setClaims(claims.toString());
        bind.setLastLoginTime(LocalDateTime.now());
    }

    /**
     * Carry the provider's profile over to the erupt user. By default only a blank field is
     * filled, so a name or avatar somebody set by hand survives; with sync on, the provider
     * is the source of truth and every mapped claim is written on every login.
     */
    private EruptUser applyProfile(EruptSso sso, JsonObject claims, EruptUser eruptUser) {
        boolean sync = Boolean.TRUE.equals(sso.getSyncProfile());
        boolean changed = this.grantRoles(sso, eruptUser);
        changed |= assign(sync, claim(claims, sso.getNameClaim()), eruptUser.getName(), eruptUser::setName);
        changed |= assign(sync, claim(claims, sso.getEmailClaim()), eruptUser.getEmail(), eruptUser::setEmail);
        changed |= assign(sync, claim(claims, sso.getPhoneClaim()), eruptUser.getPhone(), eruptUser::setPhone);
        changed |= assign(sync, claim(claims, sso.getAvatarClaim()), eruptUser.getAvatar(), eruptUser::setAvatar);
        if (changed) eruptDao.merge(eruptUser);
        return eruptUser;
    }

    /**
     * Top the user up to the provider's default roles when the row asks for it. Additive
     * only: what an administrator granted by hand, or another provider, is never revoked here.
     */
    private boolean grantRoles(EruptSso sso, EruptUser eruptUser) {
        if (!Boolean.TRUE.equals(sso.getGrantRolesOnLogin()) || null == sso.getDefaultRoles() || sso.getDefaultRoles().isEmpty()) return false;
        if (null == eruptUser.getRoles()) eruptUser.setRoles(new HashSet<>());
        return eruptUser.getRoles().addAll(sso.getDefaultRoles());
    }

    private static boolean assign(boolean overwrite, String value, String current, java.util.function.Consumer<String> setter) {
        if (StringUtils.isBlank(value) || value.equals(current)) return false;
        if (!overwrite && StringUtils.isNotBlank(current)) return false;
        setter.accept(value);
        return true;
    }

    /**
     * A user the provider vouches for but erupt has never seen. Created with no usable
     * password and only the role the provider row names, never an inherited privilege.
     */
    private EruptUser createUser(EruptSso sso, JsonObject claims, String account) {
        EruptUser eruptUser = new EruptUser();
        eruptUser.setAccount(account);
        // the rest of the profile (name, email, phone, avatar) is mapped in applyProfile
        eruptUser.setName(StringUtils.defaultIfBlank(claim(claims, sso.getNameClaim()), account));
        eruptUser.setStatus(true);
        eruptUser.setIsAdmin(false);
        eruptUser.setEncrypt(true);
        // no password is ever accepted for this account: the hash has no known preimage
        String salt = EncryptUtil.generateSalt();
        eruptUser.setSalt(salt);
        eruptUser.setEncryptType(EncryptType.SHA512);
        eruptUser.setPassword(EncryptUtil.digestSHA512Salt(Erupts.generateCode(64), salt));
        // the account never had a default password, so do not nag its owner to change one
        eruptUser.setResetPwdTime(new Date());
        // never null: the login path streams the roles, and a user with no role is a valid outcome
        eruptUser.setRoles(new HashSet<>());
        if (null != sso.getDefaultRoles()) eruptUser.getRoles().addAll(sso.getDefaultRoles());
        eruptDao.persistAndFlush(eruptUser);
        return eruptUser;
    }

    private String subject(JsonObject claims) {
        for (String key : SUBJECT_CLAIMS) {
            String value = claim(claims, key);
            if (StringUtils.isNotBlank(value)) return value;
        }
        return null;
    }

    /**
     * The claim names the provider did send, for the error shown when the configured one is
     * not among them. Names only: the values are somebody's personal data.
     */
    private static String claimNames(JsonObject claims) {
        String names = claims.entrySet().stream()
                .filter(it -> it.getValue().isJsonPrimitive() && StringUtils.isNotBlank(it.getValue().getAsString()))
                .map(Map.Entry::getKey).collect(Collectors.joining(", "));
        return names.isEmpty() ? "" : " (" + names + ")";
    }

    private static String claim(JsonObject claims, String key) {
        if (StringUtils.isBlank(key)) return null;
        JsonElement element = claims.get(key);
        if (null == element || element.isJsonNull() || !element.isJsonPrimitive()) return null;
        return element.getAsString();
    }

    // ------------------------------------------------------------------ http

    private String exchangeCode(EruptSso sso, Endpoints endpoints, String authCode, String verifier, HttpServletRequest request) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "authorization_code");
        form.put("code", authCode);
        form.put("redirect_uri", this.redirectUri(sso, request));
        form.put("code_verifier", verifier);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(endpoints.tokenUrl()))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json");
        if (endpoints.basicAuth()) {
            // RFC 6749 forbids presenting two client credentials at once, so it is one or the other
            String basic = Base64.getEncoder().encodeToString(
                    (urlEncode(sso.getClientId()) + ":" + urlEncode(sso.getClientSecret())).getBytes(StandardCharsets.UTF_8));
            builder.header("Authorization", "Basic " + basic);
        } else {
            form.put("client_id", sso.getClientId());
            form.put("client_secret", sso.getClientSecret());
        }
        JsonObject json = this.send(builder.POST(HttpRequest.BodyPublishers.ofString(formBody(form))).build(), "token");
        String accessToken = claim(json, "access_token");
        Erupts.requireTrue(StringUtils.isNotBlank(accessToken), I18nTranslate.$translate("sso.token_failed"));
        return accessToken;
    }

    private JsonObject userInfo(Endpoints endpoints, String accessToken) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoints.userInfoUrl()))
                .timeout(TIMEOUT)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET().build();
        return unwrap(this.send(request, "userinfo"));
    }

    // ------------------------------------------------------- provider flows

    /**
     * DingTalk unified login: the token endpoint wants a JSON body with camel cased names,
     * and the user endpoint wants the token in its own header rather than a bearer.
     */
    private JsonObject dingTalkIdentity(EruptSso sso, Endpoints endpoints, String authCode) {
        JsonObject body = new JsonObject();
        body.addProperty("clientId", sso.getClientId());
        body.addProperty("clientSecret", sso.getClientSecret());
        body.addProperty("code", authCode);
        body.addProperty("grantType", "authorization_code");
        JsonObject token = this.send(HttpRequest.newBuilder(URI.create(endpoints.tokenUrl())).timeout(TIMEOUT)
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build(), "token");
        String accessToken = claim(token, "accessToken");
        Erupts.requireTrue(StringUtils.isNotBlank(accessToken), I18nTranslate.$translate("sso.token_failed"));
        return this.send(HttpRequest.newBuilder(URI.create(endpoints.userInfoUrl())).timeout(TIMEOUT)
                .header("x-acs-dingtalk-access-token", accessToken).header("Accept", "application/json")
                .GET().build(), "userinfo");
    }

    /**
     * WeCom never issues a user token. The corp's own token (from the configured token URL)
     * resolves the code to a member id, and the profile is then read from the contact book,
     * which is the configured user info URL. When the code also yields a user ticket, the
     * sensitive fields WeCom withholds from the contact book are fetched with it and merged in.
     * Both extra endpoints sit next to the user info one under the same {@code /cgi-bin/}.
     */
    private JsonObject weComIdentity(EruptSso sso, Endpoints endpoints, String authCode) {
        String cgi = SsoProviderApi.cgiBase(endpoints.userInfoUrl());
        String accessToken = api.appAccessToken(sso);
        JsonObject identity = this.weComGet(cgi + "auth/getuserinfo", Map.of("access_token", accessToken, "code", authCode), "userinfo");
        String userId = claim(identity, "userid");
        // a visitor from outside the corp only has an openid, and cannot be looked up in the contact book
        Erupts.requireTrue(StringUtils.isNotBlank(userId), I18nTranslate.$translate("sso.not_member"));
        JsonObject profile = this.weComGet(cgi + "user/get", Map.of("access_token", accessToken, "userid", userId), "userinfo");
        String ticket = claim(identity, "user_ticket");
        if (StringUtils.isNotBlank(ticket)) {
            JsonObject body = new JsonObject();
            body.addProperty("user_ticket", ticket);
            JsonObject detail = SsoProviderApi.requireOk(this.send(HttpRequest.newBuilder(URI.create(cgi + "auth/getuserdetail?access_token=" + urlEncode(accessToken)))
                    .timeout(TIMEOUT).header("Content-Type", "application/json").header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build(), "userdetail"), "userdetail");
            for (Map.Entry<String, JsonElement> entry : detail.entrySet()) profile.add(entry.getKey(), entry.getValue());
        }
        return profile;
    }

    private JsonObject weComGet(String url, Map<String, String> params, String stage) {
        return api.get(url, params, stage);
    }

    /**
     * WeChat open platform: the token call is a GET with the credentials in the query and it
     * already returns the openid, which the user info GET then needs alongside the token.
     */
    private JsonObject weChatIdentity(EruptSso sso, Endpoints endpoints, String authCode) {
        Map<String, String> tokenParams = new LinkedHashMap<>();
        tokenParams.put("appid", sso.getClientId());
        tokenParams.put("secret", sso.getClientSecret());
        tokenParams.put("code", authCode);
        tokenParams.put("grant_type", "authorization_code");
        JsonObject token = this.weComGet(endpoints.tokenUrl(), tokenParams, "token");
        String accessToken = claim(token, "access_token");
        Erupts.requireTrue(StringUtils.isNotBlank(accessToken), I18nTranslate.$translate("sso.token_failed"));
        Map<String, String> userParams = new LinkedHashMap<>();
        userParams.put("access_token", accessToken);
        userParams.put("openid", StringUtils.defaultString(claim(token, "openid")));
        userParams.put("lang", "zh_CN");
        return this.weComGet(endpoints.userInfoUrl(), userParams, "userinfo");
    }

    /**
     * Some providers (Feishu, WeCom, DingTalk) do not answer with the claims themselves but
     * with an envelope: a numeric {@code code}, a message and the claims under {@code data}.
     * Open the envelope so the claim names configured on the row are looked up where they are.
     */
    static JsonObject unwrap(JsonObject json) {
        JsonElement code = json.get("code");
        JsonElement data = json.get("data");
        if (null == code || !code.isJsonPrimitive() || null == data || !data.isJsonObject()) return json;
        // a non zero code is the provider saying no, even though the HTTP status said yes
        Erupts.requireTrue(code.getAsJsonPrimitive().isNumber() && code.getAsInt() == 0,
                I18nTranslate.$translate("sso.provider_error") + " (" + code.getAsString() + ": " + claim(json, "msg") + ")");
        return data.getAsJsonObject();
    }

    private JsonObject send(HttpRequest request, String stage) {
        return api.send(request, stage);
    }

    // ------------------------------------------------------------- endpoints

    /**
     * The three URLs the flow needs, either configured outright or read once from the
     * issuer's discovery document.
     */
    private Endpoints endpoints(EruptSso sso) {
        if (StringUtils.isNoneBlank(sso.getAuthorizeUrl(), sso.getTokenUrl(), sso.getUserInfoUrl())) {
            // without a discovery document the preset is the only thing that knows how the token endpoint authenticates
            boolean basicAuth = null != sso.getType() && null != sso.getType().preset() && sso.getType().preset().isBasicAuth();
            return new Endpoints(sso.getAuthorizeUrl(), sso.getTokenUrl(), sso.getUserInfoUrl(), basicAuth);
        }
        Erupts.requireTrue(StringUtils.isNotBlank(sso.getIssuer()), I18nTranslate.$translate("sso.endpoint_missing"));
        Endpoints discovered = discoveryCache.computeIfAbsent(sso.getIssuer(), this::discover);
        return new Endpoints(
                StringUtils.defaultIfBlank(sso.getAuthorizeUrl(), discovered.authorizeUrl()),
                StringUtils.defaultIfBlank(sso.getTokenUrl(), discovered.tokenUrl()),
                StringUtils.defaultIfBlank(sso.getUserInfoUrl(), discovered.userInfoUrl()),
                discovered.basicAuth());
    }

    /**
     * Resolve the issuer's discovery document now rather than at the first login, so a
     * provider without one (Feishu, GitHub, Gitee) is caught while the admin is still
     * looking at the form. Cached like any other lookup.
     */
    public void verifyDiscovery(String issuer) {
        discoveryCache.computeIfAbsent(issuer, this::discover);
    }

    private Endpoints discover(String issuer) {
        String url = StringUtils.removeEnd(issuer, "/") + "/.well-known/openid-configuration";
        JsonObject json;
        try {
            json = this.send(HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT)
                    .header("Accept", "application/json").GET().build(), "discovery");
        } catch (EruptWebApiRuntimeException e) {
            // whatever came back, it was not a discovery document: the fix is the same either way
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("sso.discovery_failed") + " (" + url + ")");
        }
        Endpoints endpoints = new Endpoints(
                claim(json, "authorization_endpoint"),
                claim(json, "token_endpoint"),
                claim(json, "userinfo_endpoint"),
                // client_secret_post is the default; fall back to basic only when the provider says it is the only way
                !supports(json, "client_secret_post") && supports(json, "client_secret_basic"));
        Erupts.requireTrue(StringUtils.isNoneBlank(endpoints.authorizeUrl(), endpoints.tokenUrl(), endpoints.userInfoUrl()),
                I18nTranslate.$translate("sso.endpoint_missing"));
        return endpoints;
    }

    private static boolean supports(JsonObject json, String method) {
        JsonElement element = json.get("token_endpoint_auth_methods_supported");
        if (null == element || !element.isJsonArray()) return false;
        for (JsonElement item : element.getAsJsonArray()) {
            if (item.isJsonPrimitive() && method.equals(item.getAsString())) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ urls

    /**
     * The callback address the provider must know. Derived from the incoming request so a
     * single-host deployment needs no configuration, overridable for everything else.
     */
    public String redirectUri(EruptSso sso, HttpServletRequest request) {
        if (StringUtils.isNotBlank(sso.getRedirectUri())) return sso.getRedirectUri();
        return baseUrl(request) + EruptRestPath.ERUPT_API + "/sso/callback/" + sso.getCode();
    }

    /**
     * Where the browser lands once the provider is done. Never taken from the request:
     * a client supplied return address is an open redirect waiting to happen.
     */
    public String loginPageUrl(HttpServletRequest request, Map<String, String> params) {
        String page = StringUtils.defaultIfBlank(eruptAppProp.getLoginPagePath(), baseUrl(request) + "/#/passport/login");
        return appendQuery(page, params);
    }

    private static String baseUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        return url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
    }

    /**
     * Append parameters at the very end, past a hash route if there is one: the admin UI
     * routes on the hash, so a query written before the '#' never reaches the router.
     */
    private static String appendQuery(String url, Map<String, String> params) {
        if (params.isEmpty()) return url;
        int hash = url.lastIndexOf('#');
        String tail = hash < 0 ? url : url.substring(hash);
        return url + (tail.contains("?") ? "&" : "?") + formBody(params);
    }

    /**
     * The authorize URL is fetched by a browser, so its query has to sit before any fragment:
     * WeChat's in-app flow ends its URL in {@code #wechat_redirect}, and a fragment is never
     * sent to the server anyway.
     */
    private static String authorizeQuery(String url, Map<String, String> params) {
        int hash = url.indexOf('#');
        if (hash < 0) return appendQuery(url, params);
        return appendQuery(url.substring(0, hash), params) + url.substring(hash);
    }

    private static String formBody(Map<String, String> params) {
        return params.entrySet().stream().map(it -> urlEncode(it.getKey()) + "=" + urlEncode(it.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(null == value ? "" : value, StandardCharsets.UTF_8);
    }

    private static String challenge(String verifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    private EruptSso findEnabled(String code) {
        EruptSso sso = eruptDao.lambdaQuery(EruptSso.class).eq(EruptSso::getCode, code).eq(EruptSso::getStatus, true).one();
        Erupts.requireNonNull(sso, I18nTranslate.$translate("sso.provider_not_found"));
        return sso;
    }

    private record Endpoints(String authorizeUrl, String tokenUrl, String userInfoUrl, boolean basicAuth) {
    }

    @lombok.Getter
    @lombok.Setter
    private static class SsoState {

        private Long ssoId;

        private String verifier;

    }

}
