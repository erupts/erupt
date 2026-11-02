package xyz.erupt.jpa.support;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.binder.internal.TenantIdBinder;

import java.util.function.Supplier;

/**
 * The tenant restriction Hibernate puts on every query of an entity with a {@code @TenantId}
 * attribute: a session is bound to one tenant and the filter named {@link TenantIdBinder#FILTER_NAME}
 * keeps its queries inside it. This helper lifts the filter for one piece of work, so an operator
 * may list every tenant's users or a scheduler may walk every tenant, and puts it back afterwards.
 * <p>
 * Only queries are affected. Loading by id still answers {@code null} for a row of another tenant,
 * and an insert is still stamped with the session's tenant; both need a session of the right tenant.
 *
 * @author YuePeng
 */
public final class TenantFilter {

    private TenantFilter() {
    }

    /**
     * Whether the current session restricts queries to its tenant.
     */
    public static boolean isEnabled(EntityManager entityManager) {
        return null != entityManager.unwrap(Session.class).getEnabledFilter(TenantIdBinder.FILTER_NAME);
    }

    /**
     * Runs the work with the tenant restriction lifted, restoring it before returning. Without any
     * {@code @TenantId} entity (no filter defined) the work simply runs.
     */
    public static <R> R acrossTenants(EntityManager entityManager, Supplier<R> work) {
        Session session = entityManager.unwrap(Session.class);
        if (null == session.getEnabledFilter(TenantIdBinder.FILTER_NAME)) return work.get();
        session.disableFilter(TenantIdBinder.FILTER_NAME);
        try {
            return work.get();
        } finally {
            session.enableFilter(TenantIdBinder.FILTER_NAME)
                    .setParameter(TenantIdBinder.PARAMETER_NAME, session.getTenantIdentifierValue())
                    .validate();
        }
    }
}
