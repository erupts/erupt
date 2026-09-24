package xyz.erupt.sso.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.model.EruptSsoBind;
import xyz.erupt.upms.model.EruptUser;

import java.util.List;
import java.util.Optional;

/**
 * What other modules ask about a user's identity at a provider, typically to reach them
 * there: the Feishu open_id a bot message is addressed to, the WeCom userid, or any other
 * field the provider sent at the last login.
 *
 * <p>Everything here is read from the binding the login flow maintains; nothing is fetched
 * from the provider. A user who has never signed in through the provider has no binding.
 *
 * @author YuePeng
 * date 2026-09-24
 */
@Service
public class EruptSsoBindService {

    @Resource
    private EruptDao eruptDao;

    /**
     * The binding between an erupt user and one provider, by the provider's code.
     */
    public Optional<EruptSsoBind> find(Long userId, String providerCode) {
        if (null == userId || StringUtils.isBlank(providerCode)) return Optional.empty();
        EruptSso sso = eruptDao.lambdaQuery(EruptSso.class).eq(EruptSso::getCode, providerCode).one();
        if (null == sso) return Optional.empty();
        EruptUser user = eruptDao.find(EruptUser.class, userId);
        if (null == user) return Optional.empty();
        return Optional.ofNullable(eruptDao.lambdaQuery(EruptSsoBind.class)
                .eq(EruptSsoBind::getSso, sso).eq(EruptSsoBind::getEruptUser, user).one());
    }

    /**
     * Every provider the user has signed in through.
     */
    public List<EruptSsoBind> findAll(Long userId) {
        if (null == userId) return List.of();
        EruptUser user = eruptDao.find(EruptUser.class, userId);
        if (null == user) return List.of();
        return eruptDao.lambdaQuery(EruptSsoBind.class).eq(EruptSsoBind::getEruptUser, user).list();
    }

    /**
     * The identifier the provider addresses the user by, as mapped through the row's
     * "Open ID Claim". Empty when the user never signed in there or the row maps no claim.
     */
    public Optional<String> openId(Long userId, String providerCode) {
        return this.find(userId, providerCode).map(EruptSsoBind::getOpenId).filter(StringUtils::isNotBlank);
    }

    /**
     * Any top level field of the user info the provider sent at the last login, e.g. a
     * Feishu {@code tenant_key} or a WeCom {@code department}, without a column for it.
     */
    public Optional<String> claim(Long userId, String providerCode, String name) {
        return this.find(userId, providerCode).flatMap(bind -> claim(bind, name));
    }

    /**
     * Same as {@link #claim(Long, String, String)} for a binding already in hand.
     */
    public static Optional<String> claim(EruptSsoBind bind, String name) {
        if (null == bind || StringUtils.isBlank(bind.getClaims()) || StringUtils.isBlank(name)) return Optional.empty();
        JsonElement root = JsonParser.parseString(bind.getClaims());
        if (!root.isJsonObject()) return Optional.empty();
        JsonElement value = root.getAsJsonObject().get(name);
        if (null == value || value.isJsonNull()) return Optional.empty();
        return Optional.of(value.isJsonPrimitive() ? value.getAsString() : value.toString());
    }

    /**
     * The whole user info snapshot, for a caller that wants more than one field.
     */
    public Optional<JsonObject> claims(Long userId, String providerCode) {
        return this.find(userId, providerCode).map(EruptSsoBind::getClaims).filter(StringUtils::isNotBlank)
                .map(JsonParser::parseString).filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject);
    }

}
