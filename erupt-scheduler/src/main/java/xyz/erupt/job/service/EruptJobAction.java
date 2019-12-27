package xyz.erupt.job.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.job.handler.JobHandler;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.model.EruptJobLog;
import xyz.erupt.tool.EruptDao;

import javax.transaction.Transactional;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2019-12-26
 */
@Service
public class EruptJobAction implements Job {

    @Autowired
    private EruptDao eruptDao;

    @Override
    @Transactional
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        JobDataMap jobDataMap = ctx.getJobDetail().getJobDataMap();
        EruptJob eruptJob = (EruptJob) jobDataMap.get(ctx.getJobDetail().getKey().getName());
        String handler = eruptJob.getHandler();
        EruptJobLog eruptJobLog = new EruptJobLog();
        if (StringUtils.isNotBlank(handler)) {
            eruptJobLog.setEruptJob(eruptJob);
            eruptJobLog.setStartTime(new Date());

            JobHandler jobHandler = EruptSpringUtil.getBean(eruptJob.getHandler(), JobHandler.class);
            try {
                jobHandler.exec(eruptJob.getHandlerParam());
                eruptJobLog.setStatus(true);
                jobHandler.complete(true, eruptJob.getHandlerParam());
            } catch (Exception e) {
                eruptJobLog.setStatus(false);
                eruptJobLog.setErrorInfo(ExceptionUtils.getStackTrace(e));
                jobHandler.complete(false, eruptJob.getHandlerParam());
            }
            eruptJobLog.setEndTime(new Date());
//            eruptDao.persist(eruptJobLog);
        }
    }
}
