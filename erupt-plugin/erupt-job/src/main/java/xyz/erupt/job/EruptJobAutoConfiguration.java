package xyz.erupt.job;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.job.config.EruptJobProp;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.model.EruptJobLog;
import xyz.erupt.job.model.EruptMail;
import xyz.erupt.job.service.EruptJobService;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
@Component
@EnableConfigurationProperties
@Slf4j
public class EruptJobAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptJobAutoConfiguration.class);
    }

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptJobService eruptJobService;

    @Resource
    private EruptJobProp eruptJobProp;

    /**
     * ShedLock lock provider for multi-instance job dedup, created whenever redis session is enabled
     * (erupt.redis-session=true); deployments without it pay nothing. The property is the only
     * condition on purpose: a redis session already requires a RedisConnectionFactory, so if one is
     * missing the application should fail at startup with a clear message rather than at fire time.
     * A @ConditionalOnBean here would be evaluated too early whenever the host application component
     * scans xyz.erupt (a plain @ComponentScan has no AutoConfigurationExcludeFilter), and this class
     * would then be processed ahead of the Redis auto configuration and silently lose the bean.
     * {@link xyz.erupt.job.service.EruptJobAction} looks this bean up at fire time.
     */
    @Bean
    @ConditionalOnProperty(prefix = "erupt", name = "redis-session", havingValue = "true")
    public LockProvider eruptJobLockProvider(RedisConnectionFactory redisConnectionFactory) {
        return new RedisLockProvider(redisConnectionFactory);
    }

    @Override
    @SneakyThrows
    public void run() {
        if (eruptJobProp.isEnable()) {
            for (EruptJob job : eruptDao.lambdaQuery(EruptJob.class).eq(EruptJob::getStatus, true).list()) {
                try {
                    eruptJobService.modifyJob(job);
                } catch (Exception e) {
                    log.warn("The Erupt job named '{}' failed to be added: {}", job.getName(), e.getMessage());
                }
            }
        } else {
            log.info("Erupt job disable");
        }
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-job").description("Scheduled Task Management").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$job", "Job Manager", "fa fa-clock-rotate-left", 30));
        menus.add(MetaMenu.createEruptClassMenu(EruptJob.class, menus.get(0), 0));
        menus.add(MetaMenu.createEruptClassMenu(EruptJobLog.class, menus.get(0), 10));
        menus.add(MetaMenu.createEruptClassMenu(EruptMail.class, menus.get(0), 20));
        return menus;
    }
}
