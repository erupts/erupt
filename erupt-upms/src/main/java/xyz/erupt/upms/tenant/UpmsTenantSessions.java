package xyz.erupt.upms.tenant;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.helper.UpmsTenant;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Runs code against the user directory of a given tenant. A session is bound to one tenant for
 * its whole life, so acting for another tenant means a session of its own: a login that has no
 * session yet, a platform operator managing a tenant's administrator, a scheduler working through
 * every tenant.
 * <p>
 * Two shapes: {@link #callAs} hands the caller an entity manager of the tenant for direct JPA work
 * and commits it; {@link #runBound} makes the tenant's session the thread's own for the duration,
 * the way open-session-in-view does for a request, so that services written against the shared
 * {@code EruptDao} (and their {@code @Transactional} methods) run inside the tenant without knowing.
 *
 * @author YuePeng
 */
@Service
public class UpmsTenantSessions {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EntityManagerFactory entityManagerFactory;

    @Resource
    private UpmsTenantIdentifierResolver resolver;

    /**
     * @param tenantId {@code null} is the platform
     */
    public <T> T callAs(String tenantId, Function<EntityManager, T> function) {
        String tenant = UpmsTenant.normalize(tenantId);
        if (Objects.equals(tenant, resolver.resolveCurrentTenantIdentifier())) {
            return function.apply(eruptDao.getEntityManager());
        }
        try (Session session = this.openSession(tenant)) {
            Transaction transaction = session.beginTransaction();
            try {
                T result = function.apply(session);
                transaction.commit();
                return result;
            } catch (RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public void runAs(String tenantId, Consumer<EntityManager> consumer) {
        this.callAs(tenantId, em -> {
            consumer.accept(em);
            return null;
        });
    }

    /**
     * Runs the work on a thread-bound session of the tenant, so every shared entity manager use
     * inside it (EruptDao, services, their transactions) belongs to that tenant. On a thread that
     * already holds a session, the work simply runs when the session is the tenant's, and is
     * refused otherwise: a request bound to one tenant cannot act as another. Threads without a
     * session, such as schedulers, are the intended callers.
     *
     * @param tenantId {@code null} is the platform
     */
    public <T> T callBound(String tenantId, Supplier<T> work) {
        String tenant = UpmsTenant.normalize(tenantId);
        EntityManagerHolder bound = (EntityManagerHolder) TransactionSynchronizationManager.getResource(entityManagerFactory);
        if (null != bound) {
            Object current = bound.getEntityManager().unwrap(Session.class).getTenantIdentifierValue();
            if (Objects.equals(current, tenant)) return work.get();
            throw new IllegalStateException("This thread already holds a session of tenant " + current + ", it cannot act as tenant " + tenant);
        }
        // MetaContext always answers with a user object; a thread without an identity (uid and
        // tenant both null) gets a bare tenant identity for the duration, so UpmsTenant.tenantId()
        // agrees with the session, and is reset afterwards
        MetaUser user = MetaContext.getUser();
        boolean bare = null == user || (null == user.getUid() && null == user.getTenantId());
        Session session = this.openSession(tenant);
        try {
            TransactionSynchronizationManager.bindResource(entityManagerFactory, new EntityManagerHolder(session));
            if (bare) MetaContext.register(new MetaUser(null, null, null, UpmsTenant.isPlatform(tenant) ? null : tenant));
            return work.get();
        } finally {
            if (bare) MetaContext.remove();
            TransactionSynchronizationManager.unbindResource(entityManagerFactory);
            session.close();
        }
    }

    public void runBound(String tenantId, Runnable work) {
        this.callBound(tenantId, () -> {
            work.run();
            return null;
        });
    }

    private Session openSession(String tenant) {
        return entityManagerFactory.unwrap(SessionFactory.class).withOptions().tenantIdentifier(tenant).openSession();
    }
}
