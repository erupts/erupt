package xyz.erupt.job.service;

import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.simpl.SimpleThreadPool;
import org.springframework.stereotype.Service;
import xyz.erupt.job.model.EruptJob;

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
        Scheduler scheduler = schedulerFactoryMap.get(name).getScheduler();
        scheduler.triggerJob(new JobKey(name));
    }

    public void modifyJob(EruptJob eruptJob) throws SchedulerException, ParseException {
        String code = eruptJob.getCode();
        if (schedulerFactoryMap.containsKey(code)) {
            StdSchedulerFactory sf = schedulerFactoryMap.get(code);
            Scheduler ler = sf.getScheduler();
            if (!ler.isShutdown()) {
                ler.shutdown();
            }
        }
        if (eruptJob.isStatus()) {
            StdSchedulerFactory ssf = new StdSchedulerFactory();
            ssf.initialize(getSchedulerProp(code));
            Scheduler scheduler = ssf.getScheduler();
            // job
            JobDetailImpl job = new JobDetailImpl();
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(code, eruptJob);
            job.setJobDataMap(jobDataMap);
            job.setName(code);
            job.setJobClass(EruptJobAction.class);
            // trigger
            CronTriggerImpl trigger = new CronTriggerImpl();
            trigger.setName(code);
            trigger.setCronExpression(eruptJob.getCron());
            scheduler.scheduleJob(job, trigger);
            scheduler.start();
            schedulerFactoryMap.put(code, ssf);
        }
    }

    private void deleteJob(EruptJob eruptJob) throws SchedulerException {
        SchedulerFactory sf = schedulerFactoryMap.get(eruptJob.getCode());
        if (null != sf) {
            Scheduler scheduler = sf.getScheduler();
            scheduler.deleteJob(new JobKey(eruptJob.getCode()));
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
