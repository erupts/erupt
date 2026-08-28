package xyz.erupt.job.service;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.job.config.EruptJobProp;
import xyz.erupt.job.handler.EruptJobHandler;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.model.EruptJobLog;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * @author YuePeng
 * date 2019-12-26.
 */
@Slf4j
public class EruptJobAction implements Job {

    // ShedLock lock name prefix; the RedisLockProvider adds its own "job-lock" namespace on top
    public static final String JOB_KEY = "erupt-job:";

    @Override
    public void execute(JobExecutionContext ctx) {
        JobDataMap jobDataMap = ctx.getJobDetail().getJobDataMap();
        EruptJob eruptJob = (EruptJob) jobDataMap.get(ctx.getJobDetail().getKey().getName());
        JavaMailSenderImpl javaMailSender = (JavaMailSenderImpl) jobDataMap.get(EruptJobService.MAIL_SENDER_KEY);
        // Scheduled fire: dedup across instances so the job runs on exactly one node
        executeWithClusterLock(eruptJob, javaMailSender);
    }

    /**
     * Run the job under a ShedLock cluster lock so a multi-instance deployment executes it on exactly
     * one node. The lock is held for the whole execution ({@code lockAtMostFor} caps it as a crash-safety
     * net) and kept for at least {@code lockAtLeastFor} afterwards to absorb clock skew between nodes.
     * Only engaged when redis session is enabled; otherwise the job runs directly.
     */
    void executeWithClusterLock(EruptJob eruptJob, JavaMailSenderImpl javaMailSender) {
        if (!EruptSpringUtil.getBean(EruptProp.class).isRedisSession()) {
            trigger(eruptJob, javaMailSender);
            return;
        }
        EruptJobProp prop = EruptSpringUtil.getBean(EruptJobProp.class);
        LockingTaskExecutor executor = new DefaultLockingTaskExecutor(EruptSpringUtil.getBean(LockProvider.class));
        LockConfiguration lockConfig = new LockConfiguration(
                Instant.now(),
                JOB_KEY + eruptJob.getCode(),
                Duration.ofMillis(prop.getLockAtMostForMillis()),
                Duration.ofMillis(prop.getLockAtLeastForMillis()));
        try {
            LockingTaskExecutor.TaskResult<Void> result = executor.executeWithLock(
                    (LockingTaskExecutor.TaskWithResult<Void>) () -> {
                        trigger(eruptJob, javaMailSender);
                        return null;
                    }, lockConfig);
            if (!result.wasExecuted()) {
                log.info("Job [{}] skipped: cluster lock held by another instance", eruptJob.getName());
            }
        } catch (Throwable t) {
            log.error("Job [" + eruptJob.getName() + "] cluster-lock execution error", t);
        }
    }

    @SuppressWarnings("StringConcatenationArgumentToLogCall")
    void trigger(EruptJob eruptJob, JavaMailSenderImpl javaMailSender) {
        EruptJobLog eruptJobLog = new EruptJobLog();
        eruptJobLog.setJobId(eruptJob.getId());
        eruptJobLog.setStartTime(new Date());
        EruptJobHandler jobHandler = null;
        try {
            jobHandler = EruptSpringUtil.getBeanByPath(eruptJob.getHandler(), EruptJobHandler.class);
            String result = jobHandler.exec(eruptJob.getCode(), eruptJob.getHandlerParam());
            jobHandler.success(result, eruptJob.getHandlerParam());
            eruptJobLog.setResultInfo(result);
            eruptJobLog.setStatus(true);
        } catch (Exception e) {
            log.error(eruptJob.getName() + " job error", e);
            eruptJobLog.setStatus(false);
            String exceptionTraceStr = ExceptionUtils.getStackTrace(e);
            eruptJobLog.setErrorInfo(exceptionTraceStr);
            if (null != jobHandler) jobHandler.error(e, eruptJob.getHandlerParam());
            // Error Notification
            if (StringUtils.isNotBlank(eruptJob.getNotifyEmails())) {
                if (null == javaMailSender) {
                    log.warn("Sending mailbox not configured");
                } else {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setSubject(eruptJob.getName() + " job error ！！！");
                    message.setText(exceptionTraceStr);
                    message.setTo(eruptJob.getNotifyEmails().split("\\|"));
                    message.setFrom(Objects.requireNonNull(javaMailSender.getUsername()));
                    javaMailSender.send(message);
                }
            }
        }
        eruptJobLog.setHandlerParam(eruptJob.getHandlerParam());
        eruptJobLog.setEndTime(new Date());
        if (null == eruptJob.getRecordLog() || eruptJob.getRecordLog()) {
            EruptSpringUtil.getBean(EruptJobService.class).saveJobLog(eruptJobLog);
        }
    }
}
