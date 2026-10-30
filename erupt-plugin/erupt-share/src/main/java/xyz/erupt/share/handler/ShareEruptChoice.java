package xyz.erupt.share.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.share.service.EruptShareService;
import xyz.erupt.upms.service.EruptUserService;

import java.util.List;
import java.util.Map;

/**
 * The models the current user may open a public form for: those whose add right the user holds.
 * Labels are the raw {@code @Erupt(name)}; EruptShareLink is {@code @EruptI18n}, so the choice
 * list translates them afterwards.
 *
 * @author YuePeng
 */
@Component
public class ShareEruptChoice implements ChoiceFetchHandler<Void> {

    @Resource
    private EruptUserService eruptUserService;

    @Override
    public List<VLModel> fetch(String[] params) {
        Map<String, Boolean> menus = eruptUserService.getEruptMenuValuesMap();
        return EruptCoreService.getErupts().stream()
                .filter(it -> EruptShareService.canAdd(it, menus))
                .map(it -> new VLModel(it.getEruptName(), it.getErupt().name()))
                .toList();
    }
}
