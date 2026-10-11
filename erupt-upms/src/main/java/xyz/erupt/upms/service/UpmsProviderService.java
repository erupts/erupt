package xyz.erupt.upms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.spi.PlatformUpmsProvider;
import xyz.erupt.upms.spi.UpmsProvider;
import xyz.erupt.upms.spi.UpmsUser;
import xyz.erupt.upms.spi.UpmsUserContact;

import java.util.*;

/**
 * Routes every user, role, organisation and post lookup to the {@link UpmsProvider} that owns the
 * tenant.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class UpmsProviderService {

    private final List<UpmsProvider> providers = new ArrayList<>();

    public UpmsProviderService(ObjectProvider<UpmsProvider> beans, PlatformUpmsProvider platform) {
        // Spring beans and ServiceLoader registrations by @Order, the platform default last
        List<UpmsProvider> ordered = new ArrayList<>(beans.orderedStream().filter(it -> it != platform).toList());
        for (UpmsProvider provider : ServiceLoader.load(UpmsProvider.class)) {
            ordered.add(provider);
        }
        ordered.sort(AnnotationAwareOrderComparator.INSTANCE);
        ordered.add(platform);
        this.providers.addAll(ordered);
        if (this.providers.size() > 1) {
            log.info("Upms providers: {}", this.providers.stream().map(it -> it.getClass().getName()).toList());
        }
    }

    public UpmsProvider provider(String tenantId) {
        for (UpmsProvider provider : providers) {
            if (provider.supports(tenantId)) return provider;
        }
        throw new EruptWebApiRuntimeException("No UpmsProvider supports tenant: " + tenantId);
    }

    /**
     * Provider of the current session's tenant.
     */
    public UpmsProvider provider() {
        return this.provider(UpmsTenant.tenantId());
    }

    /**
     * The user, or a bare id when it no longer exists: a deleted user keeps its id on historical
     * rows and must never break a page.
     */
    public UpmsUser user(String tenantId, Long userId) {
        if (null == userId) return null;
        UpmsUser user = this.provider(tenantId).findUser(tenantId, userId);
        return null == user ? new UpmsUser(userId) : user;
    }

    /**
     * Contacts of the given users, one batch, missing users absent.
     */
    public Map<Long, UpmsUserContact> contacts(String tenantId, Collection<Long> userIds) {
        if (null == userIds || userIds.isEmpty()) return Map.of();
        return this.provider(tenantId).findContacts(tenantId, userIds);
    }

    public String userName(String tenantId, Long userId) {
        UpmsUser user = this.user(tenantId, userId);
        return null == user || null == user.getName() ? String.valueOf(userId) : user.getName();
    }

}
