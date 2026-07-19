package xyz.erupt.job.handler;

import org.quartz.CronExpression;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.job.model.EruptJob;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author YuePeng
 * date 2026-07-19
 */
@Service
public class CronPreviewButtonHandler implements EruptButtonHandler<EruptJob> {

    private static final int PREVIEW_COUNT = 5;

    @Override
    public String click(EruptJob eruptJob, String[] params) {
        if (null == eruptJob.getCron() || eruptJob.getCron().isBlank()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Cron Expression") + " " + I18nTranslate.$translate("erupt.notnull"));
        }
        CronExpression cronExpression;
        try {
            cronExpression = new CronExpression(eruptJob.getCron());
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Invalid cron expression: ") + e.getMessage());
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder result = new StringBuilder(I18nTranslate.$translate("Next execution times:"));
        Date next = new Date();
        for (int i = 0; i < PREVIEW_COUNT; i++) {
            next = cronExpression.getNextValidTimeAfter(next);
            if (null == next) break;
            result.append("\n").append(dateFormat.format(next));
        }
        return "alert(" + GsonFactory.getGson().toJson(result.toString()) + ")";
    }

}
