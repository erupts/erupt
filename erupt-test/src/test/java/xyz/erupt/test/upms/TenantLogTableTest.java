package xyz.erupt.test.upms;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.helper.UpmsSecurityHelper;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.log.EruptLoginLog;
import xyz.erupt.upms.model.log.EruptOperateLog;
import xyz.erupt.upms.model.online.EruptOnline;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.tenant.UpmsTenantSessions;

import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Login log and operation log belong to a tenant like the user directory does:
 * a tenant never lists the platform's rows nor another tenant's, and the platform never lists a
 * tenant's. A login runs before the caller has a tenant, so its log is written in the user's
 * tenant explicitly; this checks it lands where it belongs.
 */
public class TenantLogTableTest extends EruptApplicationTests {

    private static final String TENANT = "t-log-table";

    @Resource
    private UpmsTenantSessions sessions;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private PlatformTransactionManager transactionManager;

    // unique per run: the H2 database outlives a test method
    private final String mark = UUID.randomUUID().toString().substring(0, 8);

    private Long tenantUserId;

    @BeforeEach
    public void bindRequest() {
        // saveLoginLog reads the caller's user agent and address from the current request
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) Chrome/120.0");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    public void cleanUp() {
        RequestContextHolder.resetRequestAttributes();
        MetaContext.remove();
        sessions.runAs(TENANT, em -> {
            for (Class<?> type : new Class<?>[]{EruptLoginLog.class, EruptOperateLog.class}) {
                eruptDao.lambdaQuery(em, type).list().forEach(em::remove);
            }
            if (null != tenantUserId) {
                EruptUser user = em.find(EruptUser.class, tenantUserId);
                if (null != user) em.remove(user);
            }
        });
        this.inPlatformTransaction(em -> eruptDao.lambdaQuery(em, EruptLoginLog.class)
                .like(EruptLoginLog::getToken, mark).list().forEach(em::remove));
    }

    @Test
    public void aLoginIsLoggedUnderTheUsersOwnTenant() {
        tenantUserId = this.createTenantUser("log-user-" + mark);
        EruptUser tenantUser = sessions.callAs(TENANT, em -> em.find(EruptUser.class, tenantUserId));
        EruptUser platformAdmin = eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, eruptUpmsProp.getDefaultAccount()).one();
        String tenantToken = "tenant-" + mark, platformToken = "platform-" + mark;

        eruptUserService.saveLoginLog(tenantUser, tenantToken);
        eruptUserService.saveLoginLog(platformAdmin, platformToken);

        // the platform lists its own sign-in only, through both entities mapped to the table
        assertEquals(1, eruptDao.lambdaQuery(EruptLoginLog.class).like(EruptLoginLog::getToken, mark).count());
        assertEquals(UpmsTenant.PLATFORM, eruptDao.lambdaQuery(EruptLoginLog.class).eq(EruptLoginLog::getToken, platformToken).one().getTenantId());
        assertEquals(0, eruptDao.lambdaQuery(EruptOnline.class).eq(EruptOnline::getToken, tenantToken).count());
        assertEquals(2, eruptDao.lambdaQuery(EruptLoginLog.class).allTenants().like(EruptLoginLog::getToken, mark).count());

        // the tenant sees its own sign-in and nothing of the platform's
        EruptLoginLog tenantRow = sessions.callAs(TENANT, em -> eruptDao.lambdaQuery(em, EruptLoginLog.class)
                .like(EruptLoginLog::getToken, mark).one());
        assertEquals(tenantToken, tenantRow.getToken());
        assertEquals(TENANT, tenantRow.getTenantId());
        assertEquals(Long.valueOf(1), sessions.<Long>callAs(TENANT, em -> eruptDao.lambdaQuery(em, EruptOnline.class).like(EruptOnline::getToken, mark).count()));
    }

    @Test
    public void anOperationIsStampedWithItsSessionTenant() {
        String operator = "operator-" + mark;
        sessions.runAs(TENANT, em -> em.persist(this.operateLog(operator)));

        assertEquals(0, eruptDao.lambdaQuery(EruptOperateLog.class).eq(EruptOperateLog::getOperateUser, operator).count());
        assertEquals(1, eruptDao.lambdaQuery(EruptOperateLog.class).allTenants().eq(EruptOperateLog::getOperateUser, operator).count());
        EruptOperateLog row = sessions.callAs(TENANT, em -> eruptDao.lambdaQuery(em, EruptOperateLog.class)
                .eq(EruptOperateLog::getOperateUser, operator).one());
        assertEquals(TENANT, row.getTenantId());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private EruptOperateLog operateLog(String operator) {
        EruptOperateLog log = new EruptOperateLog();
        log.setOperateUser(operator);
        log.setApiName("test");
        log.setStatus(true);
        log.setCreateTime(new Date());
        return log;
    }

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

    private void inPlatformTransaction(Consumer<EntityManager> work) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> work.accept(eruptDao.getEntityManager()));
    }
}
