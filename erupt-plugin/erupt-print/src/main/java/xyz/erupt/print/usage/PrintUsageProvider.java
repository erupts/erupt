package xyz.erupt.print.usage;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.usage.EruptUsage;
import xyz.erupt.core.usage.EruptUsageProvider;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.print.model.EruptPrintConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * A print template is written against one model and reads its fields by name: the template breaks
 * quietly when a field is renamed, and only the person printing finds out.
 *
 * @author YuePeng
 */
@Service
public class PrintUsageProvider implements EruptUsageProvider {

    @Resource
    private EruptDao eruptDao;

    @Override
    public List<EruptUsage> usages() {
        String usage = I18nTranslate.$translate("Print Template");
        List<EruptUsage> usages = new ArrayList<>();
        for (EruptPrintConfig config : eruptDao.lambdaQuery(EruptPrintConfig.class).list()) {
            if (null != config.getErupt()) {
                usages.add(new EruptUsage(config.getErupt(), usage, config.getTitle(),
                        EruptUsage.tableRoute(EruptPrintConfig.class)));
            }
        }
        return usages;
    }

}
