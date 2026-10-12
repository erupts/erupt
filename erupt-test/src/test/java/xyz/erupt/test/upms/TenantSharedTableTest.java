package xyz.erupt.test.upms;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.helper.UpmsSecurityHelper;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.tenant.UpmsTenantSessions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Platform and tenant users share e_upms_user. Hibernate keeps every session inside its own
 * tenant: the platform never sees a tenant's rows, a tenant never sees the platform's, and the
 * only ways across are a session opened for the other tenant or an explicit allTenants() query.
 */
public class TenantSharedTableTest extends EruptApplicationTests {

    private static final String TENANT = "t-shared-table";

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private UpmsTenantSessions sessions;

    private Long tenantUserId;

    @AfterEach
    public void cleanUp() {
        MetaContext.remove();
        if (null != tenantUserId) {
            sessions.runAs(TENANT, em -> {
                EruptUser user = em.find(EruptUser.class, tenantUserId);
                if (null != user) em.remove(user);
            });
        }
    }

    @Test
    public void platformRowsCarryThePlatformTenant() {
        EruptUser admin = eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, eruptUpmsProp.getDefaultAccount()).one();
        assertNotNull(admin);
        assertEquals(UpmsTenant.PLATFORM, admin.getTenantId());
        // the platform convention for sessions and modules stays null
        assertNull(admin.toMetaUser().getTenantId());
    }

    @Test
    public void aTenantMayReuseAPlatformAccountAndStaysInvisibleToThePlatform() {
        String account = eruptUpmsProp.getDefaultAccount();
        tenantUserId = this.createTenantUser(account);

        // same account twice in the table, one per tenant: the platform session still sees one
        assertEquals(1, eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, account).count());
        assertEquals(2, eruptDao.lambdaQuery(EruptUser.class).allTenants().eq(EruptUser::getAccount, account).count());
        // the switch is per query, the session is restricted again afterwards
        assertEquals(1, eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, account).count());

        // loading by id does not cross tenants either, on any entity mapped to the table
        assertNull(eruptDao.find(EruptUser.class, tenantUserId));
        assertNull(eruptDao.find(EruptUserVo.class, tenantUserId));

        // a session of the tenant sees its own rows and nothing of the platform
        List<EruptUser> tenantRows = sessions.callAs(TENANT, em -> eruptDao.lambdaQuery(em, EruptUser.class).list());
        assertEquals(1, tenantRows.size());
        assertEquals(tenantUserId, tenantRows.get(0).getId());
        assertEquals(TENANT, tenantRows.get(0).getTenantId());
    }

    @Test
    public void aTenantSessionOnTheCurrentThreadIsRestrictedThroughMetaContext() {
        tenantUserId = this.createTenantUser("tenant-session-user");
        MetaContext.register(new MetaUser(tenantUserId, "tenant-session-user", "Tenant Session User", TENANT));
        List<EruptUser> rows = eruptDao.lambdaQuery(EruptUser.class).list();
        assertEquals(1, rows.size());
        assertEquals(tenantUserId, rows.get(0).getId());
        assertNull(eruptDao.find(EruptUser.class, this.platformAdminId()));
    }

    /**
     * A scheduler has no session; binding one of the tenant to the thread makes every shared
     * EruptDao call inside it the tenant's, and the thread is left as it was afterwards.
     */
    @Test
    public void aBoundTenantSessionCarriesTheSharedDaoAndIsReleasedAfterwards() {
        tenantUserId = this.createTenantUser("bound-user");
        List<EruptUser> rows = sessions.callBound(TENANT, () -> eruptDao.lambdaQuery(EruptUser.class).list());
        assertEquals(1, rows.size());
        assertEquals(tenantUserId, rows.get(0).getId());
        assertNotNull(sessions.callBound(TENANT, () -> eruptDao.find(EruptUser.class, tenantUserId)));
        // back on the platform: the tenant row is out of sight again and no tenant identity lingers
        assertNull(eruptDao.find(EruptUser.class, tenantUserId));
        assertNull(MetaContext.getUser().getTenantId());
    }


    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Long createTenantUser(String account) {
        return sessions.callAs(TENANT, em -> {
            EruptUser user = new EruptUser();
            user.setAccount(account);
            user.setName("Tenant User");
            user.setStatus(true);
            user.setIsAdmin(true);
            user.setEncrypt(true);
            UpmsSecurityHelper.applyPassword(user, "secret", true);
            em.persist(user);
            em.flush();
            return user.getId();
        });
    }

    private Long platformAdminId() {
        MetaUser current = MetaContext.getUser();
        MetaContext.remove();
        try {
            return eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, eruptUpmsProp.getDefaultAccount()).one().getId();
        } finally {
            MetaContext.register(current);
        }
    }
}
