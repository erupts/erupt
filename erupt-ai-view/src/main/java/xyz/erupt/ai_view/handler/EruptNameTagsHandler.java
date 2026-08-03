package xyz.erupt.ai_view.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.ai_view.model.AiView;
import xyz.erupt.annotation.fun.TagsFetchHandler;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Lists all registered Erupt model names as selectable tags.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Component
public class EruptNameTagsHandler implements TagsFetchHandler<AiView> {

    @Override
    public List<String> fetchTags(AiView aiView, String[] params) {
        return EruptCoreService.getErupts().stream().map(EruptModel::getEruptName).sorted().collect(Collectors.toList());
    }

}
