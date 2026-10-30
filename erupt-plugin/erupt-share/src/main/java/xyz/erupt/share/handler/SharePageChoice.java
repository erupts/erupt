package xyz.erupt.share.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptTokenService;
import xyz.erupt.upms.vo.EruptMenuVo;

import java.util.List;
import java.util.Set;

/**
 * The template pages the current user may share: the tpl menus of their session, labelled by
 * menu name. A page is reachable through its menu value, which is exactly the path the share
 * link then opens, so picking from this list is both the permission check and the input.
 *
 * @author YuePeng
 */
@Component
public class SharePageChoice implements ChoiceFetchHandler<Void> {

    // Menu types registered by erupt-tpl (desktop and micro-frontend template pages)
    private static final Set<String> TPL_TYPES = Set.of("tpl", "mtpl");

    @Resource
    private EruptTokenService eruptTokenService;

    @Resource
    private EruptContextService eruptContextService;

    @Override
    public List<VLModel> fetch(String[] params) {
        List<EruptMenuVo> menus = eruptTokenService.menuView(eruptContextService.getCurrentToken());
        if (null == menus) return List.of();
        return menus.stream()
                .filter(it -> null != it.getType() && TPL_TYPES.contains(it.getType()) && null != it.getValue())
                .map(it -> new VLModel(it.getValue(), it.getName()))
                .toList();
    }
}
