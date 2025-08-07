package xyz.erupt.job.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.job.handler.EruptJobHandler;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.model.EruptJobLog;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author YuePeng
 * date 2019-12-26.
 */
@Slf4j
public class EruptJobAction implements Job {

    public static final String JOB_KEY = "erupt-job:distributed-lock:";

    @Override
    public void execute(JobExecutionContext ctx) {
        JobDataMap jobDataMap = ctx.getJobDetail().getJobDataMap();
        EruptJob eruptJob = (EruptJob) jobDataMap.get(ctx.getJobDetail().getKey().getName());
        trigger(eruptJob, (JavaMailSenderImpl) jobDataMap.get(EruptJobService.MAIL_SENDER_KEY));
    }

    void trigger(EruptJob eruptJob, JavaMailSenderImpl javaMailSender) {
        if (EruptSpringUtil.getBean(EruptProp.class).isRedisSession()) {
            if (Boolean.FALSE.equals(EruptSpringUtil.getBean(EruptJobService.class).getStringRedisTemplate().opsForValue().setIfAbsent(JOB_KEY + eruptJob.getCode(), eruptJob.getCode(), 999, TimeUnit.MILLISECONDS))) {
                log.info("The {} task has been executed in other nodes", eruptJob.getName());
                return;
            }
        }
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
            //失败通知
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
