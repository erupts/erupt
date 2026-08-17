package xyz.erupt.monitor.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.monitor.service.EruptClassInfoDataService;

import java.util.*;

/**
 * Distinct data-processor values in use by registered @Erupt classes,
 * so the class-registry search filters by an exact processor pick.
 *
 * @author YuePeng
 */
@Component
public class EruptClassDataProcessorFetchHandler implements ChoiceFetchHandler<Void> {

    @Override
    public List<VLModel> fetch(String[] params) {
        Set<String> processors = new LinkedHashSet<>();
        for (EruptModel model : EruptCoreService.getErupts()) {
            processors.add(EruptClassInfoDataService.resolveDataProcessor(model.getClazz()));
        }
        List<String> sorted = new ArrayList<>(processors);
        sorted.sort(Comparator.naturalOrder());
        List<VLModel> list = new ArrayList<>(sorted.size());
        for (String processor : sorted) list.add(new VLModel(processor, processor));
        return list;
    }

}
