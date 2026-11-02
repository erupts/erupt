package xyz.erupt.ai_dev.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.upms.tenant.UpmsTenantSessions;

import java.util.function.Supplier;

/**
 * A short transaction in a given tenant, for background threads: each call opens its own tenant-bound
 * session, so a run that lasts an hour never holds a session or a connection between its writes.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Component
public class DevTx {

    @Resource
    private UpmsTenantSessions upmsTenantSessions;

    @Resource
    private TransactionTemplate transactionTemplate;

    public <T> T call(String tenant, Supplier<T> work) {
        return upmsTenantSessions.callBound(tenant, () -> transactionTemplate.execute(status -> work.get()));
    }

    public void run(String tenant, Runnable work) {
        this.call(tenant, () -> {
            work.run();
            return null;
        });
    }

}
