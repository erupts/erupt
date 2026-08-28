package xyz.erupt.job.service;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.job.config.EruptJobProp;
import xyz.erupt.job.handler.EruptJobHandler;
import xyz.erupt.job.model.EruptJob;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifies the multi-instance dedup lock in {@link EruptJobAction}: the job handler runs exactly when
 * it should. {@code EruptSpringUtil} static lookups are mocked, and the ShedLock {@link LockProvider}
 * is stubbed to simulate lock-acquired / lock-held-elsewhere, so no Redis or scheduler is needed.
 *
 * @author YuePeng
 */
public class EruptJobActionLockTest {

    private static final String HANDLER_PATH = "com.example.DemoHandler";

    private EruptJob job() {
        EruptJob job = new EruptJob();
        job.setId(1L);
        job.setCode("demoJob");
        job.setName("Demo Job");
        job.setHandler(HANDLER_PATH);
        job.setHandlerParam("p");
        job.setRecordLog(false); // skip the saveJobLog / EruptJobService path
        return job;
    }

    private void stubLockBeans(MockedStatic<EruptSpringUtil> spring, EruptProp prop, LockProvider lockProvider) {
        spring.when(() -> EruptSpringUtil.getBean(EruptProp.class)).thenReturn(prop);
        spring.when(() -> EruptSpringUtil.getBean(EruptJobProp.class)).thenReturn(new EruptJobProp());
        spring.when(() -> EruptSpringUtil.getBean(LockProvider.class)).thenReturn(lockProvider);
    }

    @Test
    public void runsDirectlyWhenRedisSessionDisabled() throws Exception {
        EruptProp prop = new EruptProp(); // redisSession defaults to false
        EruptJobHandler handler = mock(EruptJobHandler.class);
        when(handler.exec(anyString(), anyString())).thenReturn("ok");

        try (MockedStatic<EruptSpringUtil> spring = mockStatic(EruptSpringUtil.class)) {
            spring.when(() -> EruptSpringUtil.getBean(EruptProp.class)).thenReturn(prop);
            spring.when(() -> EruptSpringUtil.getBeanByPath(HANDLER_PATH, EruptJobHandler.class)).thenReturn(handler);

            new EruptJobAction().executeWithClusterLock(job(), null);

            verify(handler).exec("demoJob", "p");
            // Lock machinery must not be touched when redis session is off
            spring.verify(() -> EruptSpringUtil.getBean(LockProvider.class), never());
        }
    }

    @Test
    public void executesJobWhenLockAcquired() throws Exception {
        EruptProp prop = new EruptProp();
        prop.setRedisSession(true);
        EruptJobHandler handler = mock(EruptJobHandler.class);
        when(handler.exec(anyString(), anyString())).thenReturn("ok");
        LockProvider lockProvider = mock(LockProvider.class);
        SimpleLock lock = mock(SimpleLock.class);
        when(lockProvider.lock(any(LockConfiguration.class))).thenReturn(Optional.of(lock));

        try (MockedStatic<EruptSpringUtil> spring = mockStatic(EruptSpringUtil.class)) {
            stubLockBeans(spring, prop, lockProvider);
            spring.when(() -> EruptSpringUtil.getBeanByPath(HANDLER_PATH, EruptJobHandler.class)).thenReturn(handler);

            new EruptJobAction().executeWithClusterLock(job(), null);

            verify(handler).exec("demoJob", "p");
            verify(lock).unlock(); // lock released after execution
        }
    }

    @Test
    public void skipsJobWhenLockHeldByAnotherInstance() throws Exception {
        EruptProp prop = new EruptProp();
        prop.setRedisSession(true);
        EruptJobHandler handler = mock(EruptJobHandler.class);
        LockProvider lockProvider = mock(LockProvider.class);
        when(lockProvider.lock(any(LockConfiguration.class))).thenReturn(Optional.empty());

        try (MockedStatic<EruptSpringUtil> spring = mockStatic(EruptSpringUtil.class)) {
            stubLockBeans(spring, prop, lockProvider);
            spring.when(() -> EruptSpringUtil.getBeanByPath(anyString(), eq(EruptJobHandler.class))).thenReturn(handler);

            new EruptJobAction().executeWithClusterLock(job(), null);

            // Lock not acquired -> the handler must never run
            verify(handler, never()).exec(anyString(), anyString());
        }
    }

}
