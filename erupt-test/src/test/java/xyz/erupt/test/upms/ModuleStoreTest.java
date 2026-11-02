package xyz.erupt.test.upms;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.model.EruptModuleSeed;
import xyz.erupt.upms.service.EruptModuleStore;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Per-database module state in e_upms_module: one-off tasks run once, kept values stay put, and the
 * default admin account is recorded there instead of in a marker file.
 */
class ModuleStoreTest extends EruptApplicationTests {

    private static final String ONCE = "store-test-once";

    private static final String VALUE = "store-test-value";

    @Resource
    private EruptModuleStore store;

    @Resource
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status ->
                eruptDao.lambdaQuery(EruptModuleSeed.class).in(EruptModuleSeed::getModule, List.of(ONCE, VALUE)).delete());
    }

    @Test
    void aOneOffTaskRunsOncePerDatabase() {
        AtomicInteger runs = new AtomicInteger();
        assertTrue(store.once(ONCE, runs::incrementAndGet));
        assertFalse(store.once(ONCE, runs::incrementAndGet));
        assertEquals(1, runs.get());
        assertNotNull(store.find(ONCE).getSeededAt());
    }

    @Test
    void aKeptValueIsCreatedOnceAndReadBack() {
        AtomicInteger made = new AtomicInteger();
        String first = store.value(VALUE, () -> "id-" + made.incrementAndGet());
        assertEquals("id-1", first);
        assertEquals(first, store.value(VALUE, () -> "id-" + made.incrementAndGet()), "the stored value wins");
        assertEquals(1, made.get());
    }

    @Test
    void theDefaultAdminAccountIsRecordedInTheDatabase() {
        EruptModuleSeed upmsUser = store.find("erupt-upms-user");
        assertNotNull(upmsUser, "startup created the account once and marked it");
        assertNotNull(upmsUser.getSeededAt());
    }

}
