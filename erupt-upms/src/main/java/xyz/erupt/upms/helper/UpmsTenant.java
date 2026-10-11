package xyz.erupt.upms.helper;

import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptLambdaQuery;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.upms.spi.TenantScoped;

import java.util.List;
import java.util.Objects;

/**
 * Tenant of the current session, read from {@link MetaUser} which the security interceptors fill for
 * platform and tenant logins alike. {@code null} means a platform session, or no session at all
 * (inside a scheduler). Modules use it to scope their own tenant-aware rows without depending on
 * the tenant module.
 *
 * @author YuePeng
 */
public class UpmsTenant {

    public static final String CROSS_TENANT_KEY = "upms.cross_tenant_access";

    public static String tenantId() {
        MetaUser user = MetaContext.getUser();
        return null == user ? null : user.getTenantId();
    }

    /**
     * Id of the current user in the id space of the current tenant's {@code UpmsProvider}.
     */
    public static Long userId() {
        MetaUser user = MetaContext.getUser();
        return null == user ? null : user.getUid();
    }

    public static boolean sameTenant(TenantScoped scoped) {
        return Objects.equals(tenantId(), scoped.getTenantId());
    }

    /**
     * Rejects a row of another tenant. Platform sessions only see platform rows, tenant sessions only
     * rows of their own tenant. Returns the row so it can be used inline; {@code null} passes through.
     */
    public static <T extends TenantScoped> T check(T scoped) {
        if (null != scoped && !sameTenant(scoped)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate(CROSS_TENANT_KEY));
        }
        return scoped;
    }

    /**
     * The {@code DataProxy.beforeFetch} form of the tenant condition for an erupt model: a platform
     * session gets an {@code is null} clause back, a tenant session has an equality condition added
     * and gets {@code null} back, as {@code beforeFetch} expects.
     */
    public static String fetchCondition(Class<?> model, String tenantField, List<Condition> conditions) {
        String key = model.getSimpleName() + "." + tenantField;
        String tenantId = tenantId();
        if (null == tenantId) {
            return key + " is null";
        }
        conditions.add(new Condition(key, tenantId, QueryExpression.EQ));
        return null;
    }

    /**
     * Adds the tenant condition of the current session to a query: {@code is null} for the platform,
     * {@code = tenantId} for a tenant.
     */
    public static <T, E> EruptLambdaQuery<T> where(EruptLambdaQuery<T> query, SFunction<E, String> tenantField) {
        return where(query, tenantField, tenantId());
    }

    public static <T, E> EruptLambdaQuery<T> where(EruptLambdaQuery<T> query, SFunction<E, String> tenantField, String tenantId) {
        return null == tenantId ? query.isNull(tenantField) : query.eq(tenantField, tenantId);
    }

}
