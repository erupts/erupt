package xyz.erupt.remote.model.data_proxy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.context.OldEntityTL;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.remote.model.RemoteHost;
import xyz.erupt.remote.util.RemoteCrypto;

/**
 * Encrypts credentials before they are stored and keeps them out of the browser.
 * <p>
 * Erupt masks PASSWORD fields itself and keeps the stored value when the mask is echoed back.
 * The private key is a TEXTAREA, so the same masking is applied here by hand: the form receives a placeholder
 * and, when that placeholder comes back on update, the previously stored value is restored from {@link OldEntityTL}.
 *
 * @author YuePeng
 */
@Component
public class RemoteHostDataProxy implements DataProxy<RemoteHost> {

    @Resource
    private RemoteCrypto remoteCrypto;

    @Override
    public void beforeAdd(RemoteHost host) {
        requireSshUsername(host);
        host.setPassword(encryptIfPlain(host.getPassword()));
        host.setPrivateKey(encryptIfPlain(host.getPrivateKey()));
    }

    @Override
    public void beforeUpdate(RemoteHost host) {
        requireSshUsername(host);
        host.setPassword(encryptIfPlain(host.getPassword()));
        String key = host.getPrivateKey();
        if (EruptConst.PASSWORD_PLACEHOLDER.equals(key)) key = storedPrivateKey();
        host.setPrivateKey(encryptIfPlain(key));
    }

    @Override
    public void editBehavior(RemoteHost host) {
        maskPrivateKey(host);
    }

    @Override
    public void formViewBehavior(RemoteHost host) {
        maskPrivateKey(host);
    }

    private void requireSshUsername(RemoteHost host) {
        if (RemoteHost.PROTOCOL_SSH.equals(host.getProtocol()) && StringUtils.isBlank(host.getUsername())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("remote.username_required"));
        }
    }

    private void maskPrivateKey(RemoteHost host) {
        if (StringUtils.isNotBlank(host.getPrivateKey())) host.setPrivateKey(EruptConst.PASSWORD_PLACEHOLDER);
    }

    private String storedPrivateKey() {
        String json = OldEntityTL.get();
        if (json == null) return null;
        JsonObject old = GsonFactory.getGson().fromJson(json, JsonObject.class);
        JsonElement el = old.get("privateKey");
        return el == null || el.isJsonNull() ? null : el.getAsString();
    }

    private String encryptIfPlain(String value) {
        if (StringUtils.isBlank(value)) return null;
        value = value.trim();
        return remoteCrypto.isEncrypted(value) ? value : remoteCrypto.encrypt(value);
    }
}
