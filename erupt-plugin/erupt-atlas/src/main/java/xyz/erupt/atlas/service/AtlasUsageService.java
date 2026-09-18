package xyz.erupt.atlas.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.atlas.vo.Usage;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.usage.EruptUsage;
import xyz.erupt.core.usage.EruptUsageProvider;
import xyz.erupt.core.view.EruptModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * What a model is used for, asked of every module that lets a user bind one.
 * <p>
 * The graph is built from the models themselves, so it can only show what a field declares. A flow
 * bound to its form, a flow node bound to its config model, a menu opening a table, a dashboard
 * reading a cube — none of that is written on the model, and all of it breaks when the model
 * changes. Each module answers for its own bindings through {@link EruptUsageProvider}; this
 * service collects them, names the node they point at, and knows nothing else.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class AtlasUsageService {

    @Resource
    private ObjectProvider<EruptUsageProvider> usageProviders;

    @Resource
    private EruptAtlasService eruptAtlasService;

    public List<Usage> usages() {
        Map<String, String> erupts = this.eruptIds();
        Map<String, String> cubes = this.cubeIds();
        List<Usage> usages = new ArrayList<>();
        for (EruptUsageProvider provider : usageProviders.orderedStream().toList()) {
            String module = EruptAtlasService.source(provider.getClass());
            try {
                for (EruptUsage usage : provider.usages()) {
                    Map<String, String> nodes = EruptUsage.Target.CUBE == usage.type() ? cubes : erupts;
                    // A binding whose target is gone stays as it was written: the name nobody
                    // answers to is the finding, and hiding it would hide the breakage
                    String node = nodes.getOrDefault(usage.target(), usage.target());
                    usages.add(new Usage(node, module, usage.usage(), usage.owner(), usage.route()));
                }
            } catch (Exception e) {
                // One module with an unreadable table must not cost the page every other answer
                log.warn("erupt-atlas: {} could not report its usages", provider.getClass().getSimpleName(), e);
            }
        }
        return usages;
    }

    private Map<String, String> eruptIds() {
        Map<String, String> nodes = new LinkedCaseInsensitiveMap<>();
        for (EruptModel model : EruptCoreService.getErupts()) {
            nodes.put(model.getEruptName(), model.getEruptName());
        }
        return nodes;
    }

    // A cube is namespaced in the graph, because one class may be a model and a cube at once and
    // the two are used for different things: a menu opens the model, a dashboard reads the cube
    private Map<String, String> cubeIds() {
        Map<String, String> nodes = new LinkedCaseInsensitiveMap<>();
        for (Class<?> cubeClass : eruptAtlasService.cubes()) {
            nodes.put(cubeClass.getSimpleName(), EruptAtlasService.cubeId(cubeClass.getSimpleName()));
        }
        return nodes;
    }

}
