package xyz.erupt.job.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.job.handler.EruptJobHandler;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.model.EruptJobLog;

import java.util.Date;
import java.util.Objects;

/**
 * @author YuePeng
 * date 2019-12-26.
 */
@Slf4j
public class EruptJobAction implements Job {

    @Override
    public void execute(JobExecutionContext ctx) {
        JobDataMap jobDataMap = ctx.getJobDetail().getJobDataMap();
        EruptJob eruptJob = (EruptJob) jobDataMap.get(ctx.getJobDetail().getKey().getName());
        trigger(eruptJob, (JavaMailSenderImpl) jobDataMap.get(EruptJobService.MAIL_SENDER_KEY));
    }

    void trigger(EruptJob eruptJob, JavaMailSenderImpl javaMailSender) {
        String handler = eruptJob.getHandler();
        EruptJobLog eruptJobLog = new EruptJobLog();
        if (StringUtils.isNotBlank(handler)) {
            eruptJobLog.setEruptJob(eruptJob);
            eruptJobLog.setStartTime(new Date());
            EruptJobHandler jobHandler = null;
            try {
                jobHandler = EruptSpringUtil.getBeanByPath(eruptJob.getHandler(), EruptJobHandler.class);
                String result = jobHandler.exec(eruptJob.getCode(), eruptJob.getHandlerParam());
                jobHandler.success(result, eruptJob.getHandlerParam());
                eruptJobLog.setResultInfo(result);
                eruptJobLog.setStatus(true);
            } catch (Exception e) {
                e.printStackTrace();
                eruptJobLog.setStatus(false);
                String exceptionTraceStr = ExceptionUtils.getStackTrace(e);
                eruptJobLog.setErrorInfo(exceptionTraceStr);
                if (null != jobHandler) {
                    jobHandler.error(e, eruptJob.getHandlerParam());
                }
                //失败通知
                if (StringUtils.isNotBlank(eruptJob.getNotifyEmails())) {
                    if (null == javaMailSender) {
                        log.warn("Sending mailbox not configured");
                    }else{
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
            EruptSpringUtil.getBean(EruptJobService.class).saveJobLog(eruptJobLog);
        }
    }
}
