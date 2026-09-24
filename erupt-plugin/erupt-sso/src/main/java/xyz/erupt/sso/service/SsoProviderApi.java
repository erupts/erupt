package xyz.erupt.sso.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.sso.model.EruptSso;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Talking to a provider as the application rather than as a signing-in user: the HTTP
 * plumbing every provider call shares, the provider's own way of saying "no", and the
 * app-level token (Feishu tenant token, DingTalk access token, WeCom corp token) that
 * server-to-server calls such as pushing a notification are made with.
 *
 * <p>The login flow in {@link EruptSsoService} uses the same plumbing, so a provider row
 * that signs users in is also the row a notification channel sends through: one set of
 * credentials, one cache, one place that knows the provider's error envelope.
 *
 * @author YuePeng
 * date 2026-09-24
 */
@Slf4j
@Service
public class SsoProviderApi {

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(10);

    // every provider quotas its app token; it is cached until shortly before it expires
    private static final int APP_TOKEN_SAFETY_SECONDS = 60;

    private static final long DEFAULT_TOKEN_TTL_SECONDS = 7200;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(HTTP_TIMEOUT)
            .build();

    private final Map<Long, AppToken> appTokenCache = new ConcurrentHashMap<>();

    // ------------------------------------------------------------- app token

    /**
     * The token the application itself calls the provider with, fetched with the row's
     * client credentials and cached per row. Only the providers whose APIs erupt knows how
     * to drive as an app are supported; the rest have no server-to-server story here.
     */
    public String appAccessToken(EruptSso sso) {
        AppToken cached = appTokenCache.get(sso.getId());
        if (null != cached && cached.expiresAt() > System.currentTimeMillis()) return cached.value();
        AppToken token = switch (null == sso.getType() ? "" : sso.getType().name()) {
            case "FEISHU" -> this.feishuTenantToken(sso);
            case "DINGTALK" -> this.dingTalkAppToken(sso);
            case "WECOM" -> this.weComCorpToken(sso);
            default -> throw new EruptWebApiRuntimeException(I18nTranslate.$translate("sso.app_token_unsupported")
                    + " (" + sso.getCode() + ")");
        };
        appTokenCache.put(sso.getId(), token);
        return token.value();
    }

    /**
     * Forget the token cached for a provider row: its credentials may just have changed.
     */
    public void evictAppToken(Long ssoId) {
        if (null != ssoId) appTokenCache.remove(ssoId);
    }

    private AppToken feishuTenantToken(EruptSso sso) {
        JsonObject body = new JsonObject();
        body.addProperty("app_id", sso.getClientId());
        body.addProperty("app_secret", sso.getClientSecret());
        JsonObject json = this.postJson(feishuBase(sso) + "auth/v3/tenant_access_token/internal", Map.of(), body, "app token");
        return this.token(json, "tenant_access_token", claim(json, "expire"));
    }

    private AppToken dingTalkAppToken(EruptSso sso) {
        JsonObject body = new JsonObject();
        body.addProperty("appKey", sso.getClientId());
        body.addProperty("appSecret", sso.getClientSecret());
        JsonObject json = this.postJson(dingTalkBase(sso) + "oauth2/accessToken", Map.of(), body, "app token");
        return this.token(json, "accessToken", claim(json, "expireIn"));
    }

    private AppToken weComCorpToken(EruptSso sso) {
        JsonObject json = this.get(sso.getTokenUrl(), Map.of("corpid", sso.getClientId(), "corpsecret", sso.getClientSecret()), "app token");
        return this.token(json, "access_token", claim(json, "expires_in"));
    }

    private AppToken token(JsonObject json, String field, String ttl) {
        String value = claim(json, field);
        Erupts.requireTrue(StringUtils.isNotBlank(value), I18nTranslate.$translate("sso.token_failed"));
        long seconds = Math.max(0, parseLong(ttl, DEFAULT_TOKEN_TTL_SECONDS) - APP_TOKEN_SAFETY_SECONDS);
        return new AppToken(value, System.currentTimeMillis() + seconds * 1000);
    }

    // ----------------------------------------------------------- API bases

    /**
     * The {@code .../open-apis/} prefix of a Feishu API URL, taken from the row so Lark's
     * international domain works unchanged.
     */
    public static String feishuBase(EruptSso sso) {
        return prefix(sso.getUserInfoUrl(), "/open-apis/", "https://open.feishu.cn/open-apis/");
    }

    /**
     * The {@code .../v1.0/} prefix of DingTalk's new gateway, taken from the row.
     */
    public static String dingTalkBase(EruptSso sso) {
        return prefix(sso.getTokenUrl(), "/v1.0/", "https://api.dingtalk.com/v1.0/");
    }

    /**
     * DingTalk's old gateway, where the contact and message APIs still live. It is a
     * different host from the new one in production, and the same origin for anything else,
     * which is what a stand-in server in a test is.
     */
    public static String dingTalkLegacyBase(EruptSso sso) {
        String base = dingTalkBase(sso);
        if (base.startsWith("https://api.dingtalk.com/")) return "https://oapi.dingtalk.com/";
        return base.substring(0, base.indexOf("/v1.0/") + 1);
    }

    /**
     * The {@code .../cgi-bin/} prefix of a WeCom API URL, so a private deployment keeps its host.
     */
    public static String cgiBase(String url) {
        int at = null == url ? -1 : url.indexOf("/cgi-bin/");
        Erupts.requireTrue(at > 0, I18nTranslate.$translate("sso.endpoint_missing") + " (" + url + ")");
        return url.substring(0, at + "/cgi-bin/".length());
    }

    private static String prefix(String url, String marker, String fallback) {
        int at = null == url ? -1 : url.indexOf(marker);
        return at > 0 ? url.substring(0, at + marker.length()) : fallback;
    }

    // ---------------------------------------------------------------- HTTP

    public JsonObject get(String url, Map<String, String> params, String stage) {
        return requireOk(this.send(HttpRequest.newBuilder(URI.create(appendQuery(url, params))).timeout(HTTP_TIMEOUT)
                .header("Accept", "application/json").GET().build(), stage), stage);
    }

    public JsonObject postJson(String url, Map<String, String> headers, JsonObject body, String stage) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).timeout(HTTP_TIMEOUT)
                .header("Content-Type", "application/json; charset=utf-8").header("Accept", "application/json");
        headers.forEach(builder::header);
        return requireOk(this.send(builder.POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8)).build(), stage), stage);
    }

    /**
     * One round trip, answered as a JSON object or refused with the provider's own words.
     * Nothing of the body is logged: an error echo may carry the credentials back.
     */
    public JsonObject send(HttpRequest request, String stage) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                log.warn("sso {} endpoint returned {}", stage, response.statusCode());
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("sso.provider_error")
                        + " (" + stage + " HTTP " + response.statusCode() + providerError(response.body()) + ")");
            }
            JsonElement element = JsonParser.parseString(response.body());
            Erupts.requireTrue(element.isJsonObject(),
                    I18nTranslate.$translate("sso.provider_error") + " (" + stage + ": not a JSON object)");
            return element.getAsJsonObject();
        } catch (EruptWebApiRuntimeException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("sso.provider_error") + " (" + stage + ": interrupted)");
        } catch (Exception e) {
            // a connect, TLS or timeout failure: the exception type is the whole diagnosis
            log.warn("sso {} endpoint call failed", stage, e);
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("sso.provider_error")
                    + " (" + stage + ": " + e.getClass().getSimpleName() + (null == e.getMessage() ? "" : " " + e.getMessage()) + ")");
        }
    }

    /**
     * Providers that answer HTTP 200 whatever happened and report failure in the body:
     * a non zero {@code errcode} (Tencent, DingTalk's old gateway), a non zero numeric
     * {@code code} (Feishu) or {@code "ok": false} (Slack).
     */
    public static JsonObject requireOk(JsonObject json, String stage) {
        String errcode = claim(json, "errcode");
        Erupts.requireTrue(null == errcode || "0".equals(errcode), I18nTranslate.$translate("sso.provider_error")
                + " (" + stage + " " + errcode + ": " + claim(json, "errmsg") + ")");
        JsonElement code = json.get("code");
        if (null != code && code.isJsonPrimitive() && code.getAsJsonPrimitive().isNumber()) {
            Erupts.requireTrue(code.getAsInt() == 0, I18nTranslate.$translate("sso.provider_error")
                    + " (" + stage + " " + code.getAsString() + ": " + claim(json, "msg") + ")");
        }
        String ok = claim(json, "ok");
        Erupts.requireTrue(null == ok || "true".equals(ok), I18nTranslate.$translate("sso.provider_error")
                + " (" + stage + ": " + claim(json, "error") + ")");
        return json;
    }

    /**
     * The error fields providers put in a failed response: RFC 6749 style, Feishu style,
     * DingTalk style or Tencent style. Only those named fields are read, never the body as a whole.
     */
    static String providerError(String body) {
        try {
            JsonElement element = JsonParser.parseString(body);
            if (!element.isJsonObject()) return "";
            JsonObject json = element.getAsJsonObject();
            StringBuilder sb = new StringBuilder();
            for (String key : new String[]{"code", "errcode", "error", "error_description", "msg", "message", "errmsg"}) {
                String value = claim(json, key);
                if (StringUtils.isNotBlank(value)) sb.append(sb.length() == 0 ? ", " : " ").append(value);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    // --------------------------------------------------------------- utils

    public static String claim(JsonObject json, String key) {
        if (null == json || StringUtils.isBlank(key)) return null;
        JsonElement element = json.get(key);
        if (null == element || element.isJsonNull() || !element.isJsonPrimitive()) return null;
        return element.getAsString();
    }

    public static String appendQuery(String url, Map<String, String> params) {
        if (params.isEmpty()) return url;
        return url + (url.contains("?") ? "&" : "?") + formBody(params);
    }

    public static String formBody(Map<String, String> params) {
        return params.entrySet().stream().map(it -> urlEncode(it.getKey()) + "=" + urlEncode(it.getValue()))
                .collect(Collectors.joining("&"));
    }

    public static String urlEncode(String value) {
        return URLEncoder.encode(null == value ? "" : value, StandardCharsets.UTF_8);
    }

    private static long parseLong(String value, long fallback) {
        try {
            return null == value ? fallback : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private record AppToken(String value, long expiresAt) {
    }

}
