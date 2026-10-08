package xyz.erupt.ai_tune.handler;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai_tune.constants.TuneStatus;
import xyz.erupt.ai_tune.core.TuneCore;
import xyz.erupt.ai_tune.model.TuneEvent;
import xyz.erupt.ai_tune.model.TuneJob;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author YuePeng
 * date 2026/10/8
 */
@Component
public class TuneJobDataProxy implements DataProxy<TuneJob>, OnChange<TuneJob> {

    @Resource
    private EruptDao eruptDao;

    @Override
    public void beforeAdd(TuneJob job) {
        job.setStatus(TuneStatus.DRAFT);
        job.setRemoteJobId(null);
        job.setFineTunedModel(null);
        job.setRegisteredLlm(null);
        job.setErrorInfo(null);
    }

    @Override
    public void beforeUpdate(TuneJob job) {
        TuneJob old = eruptDao.find(TuneJob.class, job.getId());
        if (TuneStatus.ACTIVE.contains(old.getStatus())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.job_active"));
        }
        // Provider-owned columns survive the form round trip untouched
        job.setStatus(old.getStatus());
        job.setRemoteJobId(old.getRemoteJobId());
        job.setTrainingFileId(old.getTrainingFileId());
        job.setValidationFileId(old.getValidationFileId());
        job.setFineTunedModel(old.getFineTunedModel());
        job.setTrainedTokens(old.getTrainedTokens());
        job.setStartedAt(old.getStartedAt());
        job.setFinishedAt(old.getFinishedAt());
        job.setEstimatedFinish(old.getEstimatedFinish());
        job.setErrorInfo(old.getErrorInfo());
        job.setCheckpoints(old.getCheckpoints());
        job.setRegisteredLlm(old.getRegisteredLlm());
        eruptDao.detach(old);
    }

    @Override
    public void beforeDelete(TuneJob job) {
        TuneJob stored = eruptDao.find(TuneJob.class, job.getId());
        if (null != stored && TuneStatus.ACTIVE.contains(stored.getStatus())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.job_active"));
        }
        eruptDao.getEntityManager().createQuery("delete from TuneEvent where job.id = :id")
                .setParameter("id", job.getId()).executeUpdate();
    }

    // Picking a base LLM prefills the model id and tells whether its provider can train
    @Override
    public Map<String, Object> populateForm(TuneJob job, String[] params) {
        LLM llm = this.baseLlm(job);
        Map<String, Object> form = new HashMap<>();
        if (null != llm) form.put(LambdaSee.field(TuneJob::getBaseModel), llm.getModel());
        return form;
    }

    @Override
    public Map<String, String> buildEditExpr(TuneJob job, String[] params) {
        LLM llm = this.baseLlm(job);
        if (null == llm) return Map.of();
        String desc = null != TuneCore.get(llm.getLlm())
                ? I18nTranslate.$translate("tune.provider_ok") + " " + llm.getLlm()
                : I18nTranslate.$translate("tune.provider_unsupported") + " " + llm.getLlm()
                + " (" + String.join(", ", new TreeSet<>(TuneCore.supportedCodes())) + ")";
        return Map.of(LambdaSee.field(TuneJob::getBaseModel), "edit.desc=" + quote(desc));
    }

    private LLM baseLlm(TuneJob job) {
        return null == job.getBaseLlm() || null == job.getBaseLlm().getId() ? null
                : eruptDao.find(LLM.class, job.getBaseLlm().getId());
    }

    private static String quote(String text) {
        return "'" + StringUtils.replaceEach(text, new String[]{"\\", "'"}, new String[]{"\\\\", "\\'"}) + "'";
    }

}
