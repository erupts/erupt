package xyz.erupt.ai_tune.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_tune.model.TuneDataset;
import xyz.erupt.ai_tune.model.input.ChatHarvestForm;
import xyz.erupt.ai_tune.service.TuneDatasetService;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

import java.time.LocalDate;
import java.util.List;

/**
 * "Harvest Chat History" row operation on a dataset.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Component
public class ChatHarvestHandler implements OperationHandler<TuneDataset, ChatHarvestForm> {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TuneDatasetService datasetService;

    @Override
    public ChatHarvestForm eruptFormValue(List<TuneDataset> data, ChatHarvestForm form, String[] param) {
        form.setFromDate(LocalDate.now().minusDays(30));
        form.setToDate(LocalDate.now());
        return form;
    }

    @Override
    public String exec(List<TuneDataset> data, ChatHarvestForm form, String[] param) {
        TuneDataset dataset = eruptDao.find(TuneDataset.class, data.get(0).getId());
        int added = datasetService.harvest(dataset, form);
        return "msg.success('" + added + " " + I18nTranslate.$translate("tune.harvested_samples") + "')";
    }

}
