package xyz.erupt.sso.model.data_proxy;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.sso.constant.SsoProviderType;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.service.EruptSsoService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author YuePeng
 * date 2026-09-18
 */
@Component
public class EruptSsoDataProxy implements DataProxy<EruptSso>, OnChange<EruptSso> {

    // what a preset leaves for the admin to replace, e.g. https://<host>/realms/<realm>
    private static final Pattern PLACEHOLDER = Pattern.compile("<[^<>/]+>");

    @Resource
    private EruptSsoService eruptSsoService;

    @Override
    public void beforeAdd(EruptSso eruptSso) {
        this.requireEndpoints(eruptSso);
    }

    @Override
    public void beforeUpdate(EruptSso eruptSso) {
        this.requireEndpoints(eruptSso);
    }

    @Override
    public void afterUpdate(EruptSso eruptSso) {
        eruptSsoService.evictDiscovery(eruptSso.getIssuer());
        eruptSsoService.evictAppToken(eruptSso.getId());
    }

    @Override
    public void afterDelete(EruptSso eruptSso) {
        eruptSsoService.evictDiscovery(eruptSso.getIssuer());
        eruptSsoService.evictAppToken(eruptSso.getId());
    }

    /**
     * The provider type changed in the form: hand back the preset's endpoints, scopes and claims.
     * Those and the icon are always overwritten, since the preset is the reason the type was
     * picked. The code and name are only suggested into blank fields: an admin who already
     * labelled the button keeps their label.
     */
    @Override
    public Map<String, Object> populateForm(EruptSso eruptSso, String[] params) {
        if (null == eruptSso.getType() || null == eruptSso.getType().preset()) return Map.of();
        SsoProviderType.Preset preset = eruptSso.getType().preset();
        Map<String, Object> form = new HashMap<>();
        if (StringUtils.isBlank(eruptSso.getCode())) {
            form.put(LambdaSee.field(EruptSso::getCode), eruptSso.getType().name().toLowerCase());
        }
        if (StringUtils.isBlank(eruptSso.getName())) form.put(LambdaSee.field(EruptSso::getName), preset.getName());
        form.put(LambdaSee.field(EruptSso::getIcon), preset.getIcon());
        form.put(LambdaSee.field(EruptSso::getIssuer), preset.getIssuer());
        form.put(LambdaSee.field(EruptSso::getAuthorizeUrl), preset.getAuthorizeUrl());
        form.put(LambdaSee.field(EruptSso::getTokenUrl), preset.getTokenUrl());
        form.put(LambdaSee.field(EruptSso::getUserInfoUrl), preset.getUserInfoUrl());
        form.put(LambdaSee.field(EruptSso::getScopes), preset.getScopes());
        form.put(LambdaSee.field(EruptSso::getAccountClaim), preset.getAccountClaim());
        form.put(LambdaSee.field(EruptSso::getNameClaim), preset.getNameClaim());
        form.put(LambdaSee.field(EruptSso::getEmailClaim), preset.getEmailClaim());
        form.put(LambdaSee.field(EruptSso::getPhoneClaim), preset.getPhoneClaim());
        form.put(LambdaSee.field(EruptSso::getAvatarClaim), preset.getAvatarClaim());
        form.put(LambdaSee.field(EruptSso::getOpenIdClaim), preset.getOpenIdClaim());
        return form;
    }

    /**
     * Every console names the two credentials differently (CorpID, AppKey, Application ID...);
     * put the provider's own wording under the fields so the admin knows what to paste. Reset
     * to nothing for a preset that has no special name, or the previous hint would linger.
     */
    @Override
    public Map<String, String> buildEditExpr(EruptSso eruptSso, String[] params) {
        SsoProviderType.Preset preset = null == eruptSso.getType() ? null : eruptSso.getType().preset();
        String clientId = null == preset ? null : preset.getClientIdHint();
        String clientSecret = null == preset ? null : preset.getClientSecretHint();
        Map<String, String> expr = new HashMap<>();
        expr.put(LambdaSee.field(EruptSso::getClientId), "edit.desc=" + GsonFactory.getGson().toJson(StringUtils.defaultString(clientId)));
        expr.put(LambdaSee.field(EruptSso::getClientSecret), "edit.desc=" + GsonFactory.getGson().toJson(StringUtils.defaultString(clientSecret)));
        return expr;
    }

    /**
     * Either all three endpoints are spelled out, or the issuer has to answer for the missing
     * ones through its discovery document, which is fetched right here: a row that only fails
     * at the moment someone tries to sign in is a row nobody can debug. For the same reason a
     * preset placeholder the admin forgot to replace is caught here rather than at login.
     */
    private void requireEndpoints(EruptSso eruptSso) {
        for (String url : new String[]{eruptSso.getIssuer(), eruptSso.getAuthorizeUrl(), eruptSso.getTokenUrl(), eruptSso.getUserInfoUrl()}) {
            Erupts.requireTrue(null == url || !PLACEHOLDER.matcher(url).find(),
                    I18nTranslate.$translate("sso.placeholder_left") + " (" + url + ")");
        }
        if (StringUtils.isNoneBlank(eruptSso.getAuthorizeUrl(), eruptSso.getTokenUrl(), eruptSso.getUserInfoUrl())) return;
        Erupts.requireTrue(StringUtils.isNotBlank(eruptSso.getIssuer()), I18nTranslate.$translate("sso.endpoint_missing"));
        eruptSsoService.verifyDiscovery(eruptSso.getIssuer());
    }

}
