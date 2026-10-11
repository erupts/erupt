package xyz.erupt.upms.spi;

import xyz.erupt.core.view.KV;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SPI that maps the people a module talks about — users, roles, organisations and posts — onto a
 * concrete user directory. erupt-upms ships {@link PlatformUpmsProvider} for platform sessions
 * (tenant id {@code null}); a multi-tenant deployment registers another one that answers for its
 * tenants, so modules such as erupt-flow never depend on the tenant module itself.
 * <p>
 * Implementations are discovered as Spring beans and through {@link java.util.ServiceLoader}
 * ({@code META-INF/services/xyz.erupt.upms.spi.UpmsProvider}). For every call the first provider
 * whose {@link #supports(String)} accepts the tenant id wins; providers are ordered by Spring's
 * {@code @Order}, the built-in platform provider always comes last.
 * <p>
 * All ids are in the provider's own id space. A tenant user id may collide with a platform user id,
 * which is why every method receives the tenant id explicitly instead of reading a thread context:
 * schedulers act for every tenant without any session at all.
 *
 * @author YuePeng
 */
public interface UpmsProvider {

    /**
     * Whether this provider resolves users of the given tenant. {@code null} is the platform.
     */
    boolean supports(String tenantId);

    // ---------- users ----------

    /**
     * @return the user, or {@code null} when it does not exist
     */
    UpmsUser findUser(String tenantId, Long userId);

    /**
     * Batch variant of {@link #findUser}; missing users are simply absent from the result.
     * Override it when a single query can fetch them all.
     */
    default Map<Long, UpmsUser> findUsers(String tenantId, Collection<Long> userIds) {
        Map<Long, UpmsUser> users = new LinkedHashMap<>();
        for (Long userId : userIds) {
            UpmsUser user = this.findUser(tenantId, userId);
            if (null != user) users.put(userId, user);
        }
        return users;
    }

    /**
     * Organisation, post and roles of a user; {@code null} when the user does not exist.
     */
    UpmsUserDetail userDetail(String tenantId, Long userId);

    /**
     * Account and contact details of a user, for notifications; {@code null} when the user does
     * not exist.
     */
    UpmsUserContact findContact(String tenantId, Long userId);

    /**
     * Batch variant of {@link #findContact}; missing users are simply absent from the result.
     */
    default Map<Long, UpmsUserContact> findContacts(String tenantId, Collection<Long> userIds) {
        Map<Long, UpmsUserContact> contacts = new LinkedHashMap<>();
        for (Long userId : userIds) {
            UpmsUserContact contact = this.findContact(tenantId, userId);
            if (null != contact) contacts.put(userId, contact);
        }
        return contacts;
    }

    /**
     * Every user a picker may offer.
     */
    List<UpmsUser> users(String tenantId);

    /**
     * Users of one organisation; {@code null} org id means every user.
     */
    List<UpmsUser> usersByOrg(String tenantId, Long orgId);

    // ---------- roles / organisations / posts ----------

    List<KV<Long, String>> roles(String tenantId);

    List<KV<Long, String>> orgs(String tenantId);

    List<KV<Long, String>> posts(String tenantId);

    List<Long> userIdsByRole(String tenantId, Long roleId);

    List<Long> userIdsByOrg(String tenantId, Long orgId);

    List<Long> userIdsByPost(String tenantId, Long postId);

    /**
     * Heads of the organisation {@code level} levels above the user's own organisation
     * (0 = the user's own organisation).
     */
    List<Long> orgHeads(String tenantId, Long userId, int level);

    /**
     * Division leaders (supervisors) of the organisation {@code level} levels above the user's own.
     */
    List<Long> divisionLeaders(String tenantId, Long userId, int level);

    /**
     * An administrator of the tenant, for fallbacks such as "nobody can approve"; {@code null} when
     * the tenant has none.
     */
    Long adminUserId(String tenantId);

}
