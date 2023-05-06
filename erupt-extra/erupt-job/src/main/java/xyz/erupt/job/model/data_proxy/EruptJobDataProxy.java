package xyz.erupt.job.model.data_proxy;

import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.service.EruptJobService;

import javax.annotation.Resource;
import javax.persistence.Transient;
import java.text.ParseException;
import java.util.List;

/**
 * @author YuePeng
 * date 2022/9/8 21:59
 */
@Service
public class EruptJobDataProxy implements DataProxy<EruptJob>, OperationHandler<EruptJob, Void> {

    @Transient
    @Resource
    private EruptJobService eruptJobService;

    @Override
    public void addBehavior(EruptJob eruptJob) {
        eruptJob.setStatus(true);
    }

    @Override
    public void beforeAdd(EruptJob eruptJob) {
        if (null == eruptJob.getCode()) {
            eruptJob.setCode(Erupts.generateCode());
        }
        try {
            eruptJobService.modifyJob(eruptJob);
        } catch (SchedulerException | ParseException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    @Override
    public void beforeUpdate(EruptJob eruptJob) {
        this.beforeAdd(eruptJob);
    }

    @Override
    public void beforeDelete(EruptJob eruptJob) {
        try {
            eruptJobService.deleteJob(eruptJob);
        } catch (SchedulerException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    @Override
    public String exec(List<EruptJob> eruptJob, Void param, String[] operationParam) {
        try {
            for (EruptJob job : eruptJob) eruptJobService.triggerJob(job);
            return null;
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

}
