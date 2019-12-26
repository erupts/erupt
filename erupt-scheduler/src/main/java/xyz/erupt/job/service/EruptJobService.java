package xyz.erupt.job.service;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.simpl.SimpleThreadPool;
import org.springframework.stereotype.Service;
import xyz.erupt.job.model.EruptJobModel;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author liyuepeng
 * @date 2019-12-26
 */
@Service
public class EruptJobService {

    /**
     * 执行任务线程.
     */
    private static final String PROP_THREAD_COUNT = "org.quartz.threadPool.threadCount";
    /**
     * 执行任务线程数.
     */
    private static final int DEFAULT_THREAD_COUNT = 1;
    private Map<String, StdSchedulerFactory> schedulerFactoryMap = new HashMap<>();

    public void triggerJob(String name) throws Exception {
        try {
            Scheduler scheduler = schedulerFactoryMap.get(name).getScheduler();
            scheduler.triggerJob(new JobKey(name));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void modifyJob(EruptJobModel eruptJobModel) throws SchedulerException, ParseException {
        String code = eruptJobModel.getCode();
        if (schedulerFactoryMap.containsKey(code)) {
            StdSchedulerFactory sf = schedulerFactoryMap.get(code);
            Scheduler ler = sf.getScheduler();
            if (!ler.isShutdown()) {
                ler.shutdown();
            }
        }
        if (eruptJobModel.isStatus()) {
            StdSchedulerFactory ssf = new StdSchedulerFactory();
            ssf.initialize(getSchedulerProp(code));
            Scheduler scheduler = ssf.getScheduler();
            // job
            JobDetailImpl job = new JobDetailImpl();
            job.setName(code);
            job.setJobClass(EruptJobCallBack.class);
            // trigger
            CronTriggerImpl trigger = new CronTriggerImpl();
            trigger.setName(code);
            trigger.setCronExpression(eruptJobModel.getCron());
            scheduler.scheduleJob(job, trigger);
            scheduler.start();
            schedulerFactoryMap.put(code, ssf);
        }
    }

    private void deleteJob(EruptJobModel eruptJobModel) throws SchedulerException {
        SchedulerFactory sf = schedulerFactoryMap.get(eruptJobModel.getCode());
        if (null != sf) {
            Scheduler scheduler = sf.getScheduler();
            scheduler.deleteJob(new JobKey(eruptJobModel.getCode()));
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        }
    }

    //
    private Properties getSchedulerProp(String schedulerName) {
        Properties props = new Properties();
        props.setProperty(StdSchedulerFactory.PROP_SCHED_MAKE_SCHEDULER_THREAD_DAEMON, "true");
        props.setProperty(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN_WITH_WAIT, "true");
        props.setProperty(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN, "true");
        props.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, SimpleThreadPool.class.getName());
        props.setProperty(PROP_THREAD_COUNT, Integer.toString(DEFAULT_THREAD_COUNT));
        props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID, "AUTO");
        props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, schedulerName);
        return props;
    }
}
