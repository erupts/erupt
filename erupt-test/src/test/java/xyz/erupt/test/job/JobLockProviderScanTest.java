package xyz.erupt.test.job;

import jakarta.annotation.Resource;
import net.javacrumbs.shedlock.core.LockProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import xyz.erupt.core.annotation.EruptScan;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The job module's cluster lock must exist whenever redis session is on, however the host
 * application scans. A plain @ComponentScan over xyz.erupt has no AutoConfigurationExcludeFilter,
 * so it pulls EruptJobAutoConfiguration in as an ordinary component ahead of every auto
 * configuration; the LockProvider bean must survive that ordering.
 */
@SpringBootTest(classes = JobLockProviderScanTest.ScanJobApplication.class, properties = "erupt.redis-session=true")
public class JobLockProviderScanTest {

    @Resource
    private ApplicationContext context;

    @Test
    public void lockProviderSurvivesBeingComponentScanned() {
        assertNotNull(context.getBeanProvider(LockProvider.class).getIfAvailable(),
                "no LockProvider: every scheduled job would fail at fire time with NoSuchBeanDefinitionException");
    }

    // Static nested so the other tests' default scan (which carries TypeExcludeFilter) leaves it alone
    @SpringBootApplication
    @ComponentScan("xyz.erupt.job")
    @EruptScan
    @EntityScan
    static class ScanJobApplication {
    }

}
