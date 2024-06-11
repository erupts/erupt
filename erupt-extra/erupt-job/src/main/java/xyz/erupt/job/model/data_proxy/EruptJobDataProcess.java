package xyz.erupt.job.model.data_proxy;

import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.service.EruptJobService;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import javax.persistence.Transient;
import java.text.ParseException;
import java.util.List;

/**
 * @author YuePeng
 * date 2022/9/8 21:59
 */
@Service
public class EruptJobDataProcess implements DataProxy<EruptJob>, OperationHandler<EruptJob, EruptJobExecDialog> {

    @Transient
    @Resource
    private EruptJobService eruptJobService;

    @Resource
    private EruptDao eruptDao;

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
    public String exec(List<EruptJob> eruptJob, EruptJobExecDialog param, String[] operationParam) {
        try {
            eruptDao.getEntityManager().clear();
            for (EruptJob job : eruptJob) {
                job.setHandlerParam(param.getParam());
                eruptJobService.triggerJob(job);
            }
            return null;
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    @Override
    public EruptJobExecDialog eruptFormValue(List<EruptJob> data, EruptJobExecDialog eruptJobExecDialog, String[] param) {
        eruptJobExecDialog.setParam(data.get(0).getHandlerParam());
        return eruptJobExecDialog;
    }
}
