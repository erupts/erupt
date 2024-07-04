package xyz.erupt.job.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.simpl.SimpleThreadPool;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.model.EruptJobLog;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YuePeng
 * date 2019-12-26
 */
@Service
public class EruptJobService implements DisposableBean {

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

    @Resource
    @Getter
    StringRedisTemplate stringRedisTemplate;

    public static final String MAIL_SENDER_KEY = "mailSensor";

    private final Map<String, EruptJobStdSchedulerFactory> schedulerFactoryMap = new ConcurrentHashMap<>();

    public void triggerJob(EruptJob eruptJob) {
        new EruptJobAction().trigger(eruptJob, javaMailSender);
    }

    public synchronized void addJob(EruptJob eruptJob) throws SchedulerException, ParseException {
        String code = eruptJob.getCode();
        if (!schedulerFactoryMap.containsKey(code)) {
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
                schedulerFactoryMap.put(code, new EruptJobStdSchedulerFactory(eruptJob, ssf));
            }
        }
    }

    public synchronized void modifyJob(EruptJob eruptJob) throws SchedulerException, ParseException {
        if (schedulerFactoryMap.containsKey(eruptJob.getCode())) {
            if (eruptJob.equals(schedulerFactoryMap.get(eruptJob.getCode()).getEruptJob())) {
                return;
            } else {
                this.deleteJob(eruptJob);
            }
        }
        this.addJob(eruptJob);
    }

    public synchronized void deleteJob(EruptJob eruptJob) throws SchedulerException {
        EruptJobStdSchedulerFactory sf = schedulerFactoryMap.get(eruptJob.getCode());
        if (null != sf) {
            Scheduler scheduler = sf.getStdSchedulerFactory().getScheduler();
            scheduler.deleteJob(new JobKey(eruptJob.getCode()));
            if (!scheduler.isShutdown()) scheduler.shutdown();
            schedulerFactoryMap.remove(eruptJob.getCode());
        }
    }

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

    public void saveJobLog(EruptJobLog eruptJobLog) {
        eruptDao.persistAndFlush(eruptJobLog);
    }

    @Override
    public void destroy() throws SchedulerException {
        for (EruptJobStdSchedulerFactory value : schedulerFactoryMap.values()) {
            value.getStdSchedulerFactory().getScheduler().shutdown();
        }
    }


    @Getter
    @Setter
    @NoArgsConstructor
    private static class EruptJobStdSchedulerFactory {

        private EruptJob eruptJob;

        private StdSchedulerFactory stdSchedulerFactory;

        public EruptJobStdSchedulerFactory(EruptJob eruptJob, StdSchedulerFactory stdSchedulerFactory) {
            this.eruptJob = eruptJob;
            this.stdSchedulerFactory = stdSchedulerFactory;
        }
    }

}
