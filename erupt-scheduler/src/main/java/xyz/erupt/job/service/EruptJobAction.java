package xyz.erupt.job.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.job.handler.JobHandler;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.model.EruptJobLog;

import java.util.Date;

/**
 * @author liyuepeng
 * @date 2019-12-26
 */
public class EruptJobAction implements Job {

    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        JobDataMap jobDataMap = ctx.getJobDetail().getJobDataMap();
        EruptJob eruptJob = (EruptJob) jobDataMap.get(ctx.getJobDetail().getKey().getName());
        trigger(eruptJob);
    }

    void trigger(EruptJob eruptJob) {
        String handler = eruptJob.getHandler();
        EruptJobLog eruptJobLog = new EruptJobLog();
        if (StringUtils.isNotBlank(handler)) {
            eruptJobLog.setEruptJob(eruptJob);
            eruptJobLog.setStartTime(new Date());
            JobHandler jobHandler = null;
            try {
                jobHandler = EruptSpringUtil.getBean(eruptJob.getHandler(), JobHandler.class);
                String result = jobHandler.exec(eruptJob.getHandlerParam());
                jobHandler.success(result, eruptJob.getHandlerParam());
                eruptJobLog.setResultInfo(result);
                eruptJobLog.setStatus(true);
            } catch (Exception e) {
                e.printStackTrace();
                eruptJobLog.setStatus(false);
                eruptJobLog.setErrorInfo(ExceptionUtils.getStackTrace(e));
                if (null != jobHandler) {
                    jobHandler.error(e, eruptJob.getHandlerParam());
                }
            }
            eruptJobLog.setHandlerParam(eruptJob.getHandlerParam());
            eruptJobLog.setEndTime(new Date());
            EruptSpringUtil.getBean(EruptJobService.class).saveJobLog(eruptJobLog);
        }
    }
}
