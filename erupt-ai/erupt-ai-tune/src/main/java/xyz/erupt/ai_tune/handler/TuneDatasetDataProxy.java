package xyz.erupt.ai_tune.handler;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_tune.constants.DatasetStatus;
import xyz.erupt.ai_tune.constants.TuneStatus;
import xyz.erupt.ai_tune.model.TuneDataset;
import xyz.erupt.ai_tune.model.TuneJob;
import xyz.erupt.ai_tune.service.TuneDatasetService;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;
import java.util.Objects;

/**
 * @author YuePeng
 * date 2026/10/8
 */
@Component
public class TuneDatasetDataProxy implements DataProxy<TuneDataset>, OperationHandler<TuneDataset, Void> {

    // Whether the pending update swapped the attached file (decided in beforeUpdate, consumed in afterUpdate)
    private static final ThreadLocal<Boolean> FILE_CHANGED = new ThreadLocal<>();

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TuneDatasetService datasetService;

    @Override
    public void beforeAdd(TuneDataset dataset) {
        dataset.setStatus(DatasetStatus.PENDING);
        dataset.setSampleCount(null);
        dataset.setValidCount(null);
        dataset.setTokenEstimate(null);
        dataset.setErrorInfo(null);
    }

    @Override
    public void afterAdd(TuneDataset dataset) {
        if (StringUtils.isNotBlank(dataset.getAttachment())) datasetService.ingestAsync(dataset.getId());
    }

    @Override
    public void beforeUpdate(TuneDataset dataset) {
        TuneDataset old = eruptDao.find(TuneDataset.class, dataset.getId());
        boolean changed = !Objects.equals(old.getAttachment(), dataset.getAttachment())
                || !Objects.equals(old.getFormat(), dataset.getFormat());
        // Derived columns are owned by the service, never by the form
        dataset.setStatus(old.getStatus());
        dataset.setSampleCount(old.getSampleCount());
        dataset.setValidCount(old.getValidCount());
        dataset.setTokenEstimate(old.getTokenEstimate());
        dataset.setErrorInfo(old.getErrorInfo());
        eruptDao.detach(old);
        FILE_CHANGED.set(changed);
    }

    // A new file or a format switch re-imports and re-validates; a rename does not
    @Override
    public void afterUpdate(TuneDataset dataset) {
        Boolean changed = FILE_CHANGED.get();
        FILE_CHANGED.remove();
        if (Boolean.TRUE.equals(changed)) datasetService.ingestAsync(dataset.getId());
    }

    @Override
    public void beforeDelete(TuneDataset dataset) {
        long active = eruptDao.lambdaQuery(TuneJob.class).in(TuneJob::getStatus, TuneStatus.ACTIVE)
                .addCondition("(trainingDataset.id = :dsId or validationDataset.id = :dsId)")
                .addParam("dsId", dataset.getId()).count();
        if (active > 0) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.dataset_in_use"));
        }
        datasetService.deleteSamples(dataset.getId());
    }

    // "Re-validate" row operation
    @Override
    public String exec(List<TuneDataset> data, Void unused, String[] param) {
        data.forEach(dataset -> datasetService.revalidate(dataset.getId()));
        return null;
    }

}
