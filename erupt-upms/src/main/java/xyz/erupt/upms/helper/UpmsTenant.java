package xyz.erupt.upms.helper;

import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;

/**
 * Tenant of the current session, read from {@link MetaUser} which the security interceptors fill for
 * platform and tenant logins alike. {@code null} means a platform session, or no session at all
 * (inside a scheduler).
 * <p>
 * The user directory (users, roles, organisations, posts) stores the platform under
 * {@link #PLATFORM} rather than {@code null}: Hibernate's tenant filter needs a value to compare
 * with, and a {@code null} inside a composite unique key is treated differently by every database.
 * {@link #normalize} and {@link #isPlatform} translate between the two conventions.
 *
 * @author YuePeng
 */
public class UpmsTenant {

    /**
     * Tenant id of the platform in the user directory tables.
     */
    public static final String PLATFORM = "0";

    /**
     * Key the tenant module registers in {@code EruptAppProp.properties} when it is on the classpath;
     * without it every directory row belongs to the platform.
     */
    public static final String TENANT_MODULE = "erupt-tenant";

    public static String tenantId() {
        MetaUser user = MetaContext.getUser();
        return null == user ? null : user.getTenantId();
    }

    /**
     * The directory form of a tenant id: {@code null} becomes {@link #PLATFORM}.
     */
    public static String normalize(String tenantId) {
        return null == tenantId ? PLATFORM : tenantId;
    }

    public static boolean isPlatform(String tenantId) {
        return null == tenantId || PLATFORM.equals(tenantId);
    }

    /**
     * Id of the current user. Ids are unique across the platform and every tenant, since they all
     * share the user table.
     */
    public static Long userId() {
        MetaUser user = MetaContext.getUser();
        return null == user ? null : user.getUid();
    }

}
