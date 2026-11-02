package xyz.erupt.upms.tenant;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Map;

/**
 * Tells Hibernate which tenant a session belongs to. Every entity carrying a {@code @TenantId}
 * attribute (users, roles, organisations, posts) is then restricted to that tenant on every
 * query, and stamped with it on insert, without a single condition in erupt's own code.
 * <p>
 * The platform is the tenant {@link UpmsTenant#PLATFORM}; it is an ordinary tenant, not a root
 * that sees everything, so a platform session never meets a tenant's rows by accident. Code that
 * has to act for another tenant opens a session for it through {@link UpmsTenantSessions}.
 * <p>
 * Hibernate asks once per session, and with open-session-in-view that is at the very start of a
 * request, before the security interceptor has verified the token and filled {@link MetaContext}.
 * The session token is already on the request, though, so the tenant is read from the session
 * store directly; {@link MetaContext} serves threads without a request (schedulers, async work).
 *
 * @author YuePeng
 */
@Component
public class UpmsTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    @Override
    public String resolveCurrentTenantIdentifier() {
        // MetaContext always answers with a user object; only one that carries an identity counts
        MetaUser user = MetaContext.getUser();
        if (null != user && (null != user.getUid() || null != user.getTenantId())) return UpmsTenant.normalize(user.getTenantId());
        // Without the tenant module every row is the platform's, so the session store is not
        // consulted at all: a deployment that never heard of tenants pays nothing per request
        if (EruptSpringUtil.isMvcContext() && this.tenantModulePresent()) {
            // the beans are looked up late: this resolver is built before the entity manager factory
            String token = EruptSpringUtil.getBean(EruptContextService.class).getCurrentToken();
            if (null != token) {
                MetaUserinfo userinfo = EruptSpringUtil.getBean(EruptUserService.class).getSimpleUserInfoByToken(token);
                if (null != userinfo) return UpmsTenant.normalize(userinfo.getTenantId());
            }
        }
        return UpmsTenant.PLATFORM;
    }

    private boolean tenantModulePresent() {
        return EruptSpringUtil.getBean(EruptAppProp.class).getProperties().containsKey(UpmsTenant.TENANT_MODULE);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
