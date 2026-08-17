package xyz.erupt.monitor.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptRemoteRouter;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.monitor.service.EruptClassInfoDataService;

import java.util.*;

/**
 * Distinct source values across all registered @Erupt classes (local + remote),
 * so the class-registry search can filter by a picked module/jar instead of typing a LIKE fragment.
 *
 * @author YuePeng
 */
@Component
public class EruptClassSourceFetchHandler implements ChoiceFetchHandler<Void> {

    @Override
    public List<VLModel> fetch(String[] params) {
        Set<String> sources = new LinkedHashSet<>();
        for (EruptModel model : EruptCoreService.getErupts()) {
            String source = EruptClassInfoDataService.resolveSource(model.getClazz());
            if (null != source) sources.add(source);
        }
        EruptRemoteRouter router = EruptRemoteRouterManager.get();
        if (null != router) {
            for (String remoteName : router.remoteEruptNames()) {
                int dot = remoteName.lastIndexOf(EruptConst.DOT);
                if (dot > 0) sources.add(remoteName.substring(0, dot));
            }
        }
        List<String> sorted = new ArrayList<>(sources);
        sorted.sort(Comparator.naturalOrder());
        List<VLModel> list = new ArrayList<>(sorted.size());
        for (String source : sorted) list.add(new VLModel(source, source));
        return list;
    }

}
