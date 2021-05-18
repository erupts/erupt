package xyz.erupt.job.service;

import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.simpl.SimpleThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.model.EruptJobLog;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author YuePeng
 * date 2019-12-26
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

    @Resource
    private EruptDao eruptDao;

    @Autowired(required = false)
    private JavaMailSenderImpl javaMailSender;

    public static final String MAIL_SENDER_KEY = "mailSensor";

    private final Map<String, StdSchedulerFactory> schedulerFactoryMap = new HashMap<>();

    public void triggerJob(EruptJob eruptJob) {
        new EruptJobAction().trigger(eruptJob, javaMailSender);
    }

    public void modifyJob(EruptJob eruptJob) throws SchedulerException, ParseException {
        String code = eruptJob.getCode();
        if (schedulerFactoryMap.containsKey(code)) {
            deleteJob(eruptJob);
        }
        if (eruptJob.getStatus()) {
            StdSchedulerFactory ssf = new StdSchedulerFactory();
            ssf.initialize(getSchedulerProp(code));
            Scheduler scheduler = ssf.getScheduler();
            // job
            JobDetailImpl job = new JobDetailImpl();
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(code, eruptJob);
            jobDataMap.put(MAIL_SENDER_KEY, javaMailSender);
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

    public void deleteJob(EruptJob eruptJob) throws SchedulerException {
        SchedulerFactory sf = schedulerFactoryMap.get(eruptJob.getCode());
        if (null != sf) {
            Scheduler scheduler = sf.getScheduler();
            scheduler.deleteJob(new JobKey(eruptJob.getCode()));
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
            schedulerFactoryMap.remove(eruptJob.getCode());
        }
    }

    //
    private Properties getSchedulerProp(String schedulerName) {
        Properties props = new Properties();
        props.setProperty(StdSchedulerFactory.PROP_SCHED_MAKE_SCHEDULER_THREAD_DAEMON, "true");
        props.setProperty(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN_WITH_WAIT, "true");
        props.setProperty(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN, "true");
        props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID, "AUTO");
        props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, schedulerName);
        props.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, SimpleThreadPool.class.getName());
        props.setProperty(PROP_THREAD_COUNT, Integer.toString(DEFAULT_THREAD_COUNT));
        return props;
    }

    @Transactional
    public void saveJobLog(EruptJobLog eruptJobLog) {
        eruptDao.persist(eruptJobLog);
    }
}
