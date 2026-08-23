package xyz.erupt.ai_staff.proxy;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import xyz.erupt.ai_staff.model.AiStaffTask;
import xyz.erupt.ai_staff.service.AiStaffScheduler;
import xyz.erupt.ai_staff.service.AiStaffService;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;

import java.util.List;

/**
 * Keeps the scheduler in sync with task CRUD and backs the "Execute Now" row operation.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Service
public class AiStaffTaskProxy implements DataProxy<AiStaffTask>, OperationHandler<AiStaffTask, Void> {

    @Resource
    private AiStaffScheduler aiStaffScheduler;

    @Resource
    private AiStaffService aiStaffService;

    @Override
    public void beforeAdd(AiStaffTask task) {
        if (StringUtils.isNotBlank(task.getCron())) {
            try {
                CronExpression.parse(task.getCron());
            } catch (Exception e) {
                throw new EruptWebApiRuntimeException("Cron error " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void beforeUpdate(AiStaffTask task) {
        this.beforeAdd(task);
    }

    @Override
    public void afterAdd(AiStaffTask task) {
        aiStaffScheduler.refresh(task);
    }

    @Override
    public void afterUpdate(AiStaffTask task) {
        aiStaffScheduler.refresh(task);
    }

    @Override
    public void afterDelete(AiStaffTask task) {
        aiStaffScheduler.cancel(task.getId());
    }

    @Override
    public String exec(List<AiStaffTask> data, Void unused, String[] param) {
        data.forEach(task -> aiStaffService.executeAsync(task.getId()));
        return "alert(" + GsonFactory.getGson().toJson(
                I18nTranslate.$translate("Task dispatched, see Work Log for the result")) + ")";
    }

}
