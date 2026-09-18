package xyz.erupt.sso.model.data_proxy;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.service.EruptSsoService;

/**
 * @author YuePeng
 * date 2026-09-18
 */
@Component
public class EruptSsoDataProxy implements DataProxy<EruptSso> {

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
    }

    @Override
    public void afterDelete(EruptSso eruptSso) {
        eruptSsoService.evictDiscovery(eruptSso.getIssuer());
    }

    /**
     * Either the issuer answers for the endpoints or all three are spelled out; half of each
     * is a row that only fails at the moment someone tries to sign in.
     */
    private void requireEndpoints(EruptSso eruptSso) {
        Erupts.requireTrue(StringUtils.isNotBlank(eruptSso.getIssuer())
                        || StringUtils.isNoneBlank(eruptSso.getAuthorizeUrl(), eruptSso.getTokenUrl(), eruptSso.getUserInfoUrl()),
                I18nTranslate.$translate("sso.endpoint_missing"));
    }

}
