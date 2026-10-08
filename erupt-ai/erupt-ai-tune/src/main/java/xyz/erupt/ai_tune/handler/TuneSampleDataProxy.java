package xyz.erupt.ai_tune.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_tune.model.TuneDataset;
import xyz.erupt.ai_tune.model.TuneSample;
import xyz.erupt.ai_tune.service.TuneDatasetService;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

/**
 * Keeps hand-edited samples validated and the dataset totals current.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Component
public class TuneSampleDataProxy implements DataProxy<TuneSample> {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TuneDatasetService datasetService;

    @Override
    public void beforeAdd(TuneSample sample) {
        TuneDataset dataset = this.dataset(sample);
        sample.setSeq(datasetService.nextSeq(dataset));
        sample.setSource(TuneDatasetService.SOURCE_MANUAL);
        datasetService.apply(sample, dataset.getFormat());
    }

    @Override
    public void afterAdd(TuneSample sample) {
        datasetService.refreshStats(sample.getDataset().getId());
    }

    @Override
    public void beforeUpdate(TuneSample sample) {
        TuneSample old = eruptDao.find(TuneSample.class, sample.getId());
        sample.setSeq(old.getSeq());
        sample.setSource(old.getSource());
        eruptDao.detach(old);
        datasetService.apply(sample, this.dataset(sample).getFormat());
    }

    @Override
    public void afterUpdate(TuneSample sample) {
        datasetService.refreshStats(sample.getDataset().getId());
    }

    @Override
    public void afterDelete(TuneSample sample) {
        if (null != sample.getDataset()) datasetService.refreshStats(sample.getDataset().getId());
    }

    // The form posts the reference as an id-only shell; the format lives on the stored row
    private TuneDataset dataset(TuneSample sample) {
        TuneDataset dataset = null == sample.getDataset() || null == sample.getDataset().getId() ? null
                : eruptDao.find(TuneDataset.class, sample.getDataset().getId());
        if (null == dataset) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.sample_no_dataset"));
        return dataset;
    }

}
