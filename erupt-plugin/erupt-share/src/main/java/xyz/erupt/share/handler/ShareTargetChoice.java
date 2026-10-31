package xyz.erupt.share.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.share.model.EruptShareLink;
import xyz.erupt.share.service.EruptShareService;

import java.util.ArrayList;
import java.util.List;

/**
 * What the current user may share, linked to the type chosen in the same form: for a form the
 * models whose add right they hold, for a page the menus they hold. Labels are raw names; the link
 * model is {@code @EruptI18n} and translates them afterwards.
 *
 * @author YuePeng
 */
@Component
public class ShareTargetChoice implements ChoiceFetchHandler<EruptShareLink> {

    @Resource
    private EruptShareService eruptShareService;

    // Contract: the superset of both types' targets, so a stored value always resolves to a label
    @Override
    public List<VLModel> fetch(String[] params) {
        List<VLModel> all = new ArrayList<>(eruptShareService.formTargets());
        all.addAll(eruptShareService.menuTargets());
        return all;
    }

    @Override
    public List<VLModel> fetchFilter(EruptShareLink link, String[] params) {
        if (null == link || null == link.getType()) return this.fetch(params);
        return link.isForm() ? eruptShareService.formTargets() : eruptShareService.menuTargets();
    }
}
